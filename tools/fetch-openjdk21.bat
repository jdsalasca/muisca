@echo off
setlocal enabledelayedexpansion

set ROOT_DIR=%~dp0..
set JDK_DIR=%ROOT_DIR%\.toolchains\openjdk21
set ARCHIVE=%ROOT_DIR%\.cache\openjdk21.zip

echo [muisca] Creating cache directory...
if not exist "%ROOT_DIR%\.cache" mkdir "%ROOT_DIR%\.cache"

echo [muisca] Fetching OpenJDK 21 download URL from Adoptium API...
for /f "tokens=*" %%i in ('powershell -Command "try { $url = 'https://api.adoptium.net/v3/assets/latest/21/hotspot?os=windows^&architecture=x64^&image_type=jdk'; $response = Invoke-WebRequest -Uri $url -UseBasicParsing; $json = $response.Content | ConvertFrom-Json; if ($json -and $json.Count -gt 0) { $json[0].binary.package.link } else { '' } } catch { '' }"') do set DOWNLOAD_URL=%%i

if "!DOWNLOAD_URL!"=="" (
    echo Error: Could not fetch download URL from Adoptium API
    echo Trying fallback URL...
    set DOWNLOAD_URL=https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.2%%2B13/OpenJDK21U-jdk_x64_windows_hotspot_21.0.2_13.zip
)

echo [muisca] Downloading OpenJDK 21 from !DOWNLOAD_URL!
curl -L "!DOWNLOAD_URL!" -o "%ARCHIVE%"

if errorlevel 1 (
    echo Error: Failed to download OpenJDK 21
    echo Please download manually from: https://adoptium.net/temurin/releases/?version=21
    echo Extract to: %JDK_DIR%
    exit /b 1
)

echo [muisca] Checking download size...
for %%A in ("%ARCHIVE%") do set SIZE=%%~zA
if !SIZE! LSS 1000000 (
    echo Error: Downloaded file is too small ^(!SIZE! bytes^), probably an error page
    echo Please download manually from: https://adoptium.net/temurin/releases/?version=21
    echo Extract to: %JDK_DIR%
    del "%ARCHIVE%"
    exit /b 1
)

echo [muisca] Removing existing JDK 21 directory...
if exist "%JDK_DIR%" (
    echo Removing old JDK files...
    powershell -Command "if (Test-Path '%JDK_DIR%') { Remove-Item -Path '%JDK_DIR%' -Recurse -Force -ErrorAction SilentlyContinue }"
    timeout /t 1 /nobreak >nul
)
if not exist "%JDK_DIR%" mkdir "%JDK_DIR%"

echo [muisca] Extracting OpenJDK 21...
set EXTRACT_DIR=%ROOT_DIR%\.toolchains\extract_temp
if exist "%EXTRACT_DIR%" rmdir /s /q "%EXTRACT_DIR%"
mkdir "%EXTRACT_DIR%"

powershell -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%EXTRACT_DIR%' -Force"

echo [muisca] Finding JDK directory structure...
set JDK_FOUND=0
set JDK_SOURCE=

rem Check for common JDK directory patterns
if exist "%EXTRACT_DIR%\jdk-21*" (
    for /d %%d in ("%EXTRACT_DIR%\jdk-21*") do (
        set JDK_SOURCE=%%d
        set JDK_FOUND=1
        goto :copy_jdk
    )
)

if exist "%EXTRACT_DIR%\jdk" (
    set JDK_SOURCE=%EXTRACT_DIR%\jdk
    set JDK_FOUND=1
    goto :copy_jdk
)

for /d %%d in ("%EXTRACT_DIR%\*") do (
    if exist "%%d\bin\java.exe" (
        set JDK_SOURCE=%%d
        set JDK_FOUND=1
        goto :copy_jdk
    )
)

:copy_jdk
if !JDK_FOUND!==1 (
    echo [muisca] Copying JDK from !JDK_SOURCE! to %JDK_DIR%...
    xcopy /E /I /Y "!JDK_SOURCE!\*" "%JDK_DIR%\" >nul
    if exist "%JDK_DIR%\bin\java.exe" (
        echo [muisca] JDK 21 successfully extracted to %JDK_DIR%
    ) else (
        echo Error: JDK extraction incomplete - java.exe not found
        exit /b 1
    )
) else (
    echo Error: Could not find JDK directory in extracted archive
    echo Archive contents:
    dir "%EXTRACT_DIR%"
    exit /b 1
)

rem Cleanup
rmdir /s /q "%EXTRACT_DIR%"

echo [muisca] JDK 21 installation complete!
echo Gradle will use this JDK automatically (configured in gradle.properties)
