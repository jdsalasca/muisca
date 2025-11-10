@echo off
setlocal enabledelayedexpansion

set ROOT_DIR=%~dp0..
set JDK_DIR=%ROOT_DIR%\.toolchains\openjdk25
set ARCHIVE=%ROOT_DIR%\.cache\openjdk25.zip
set URL=https://builds.shipilev.net/openjdk-jdk25/openjdk-jdk25-windows-x86_64-server.zip

echo [muisca] Creating cache directory...
if not exist "%ROOT_DIR%\.cache" mkdir "%ROOT_DIR%\.cache"

echo [muisca] Downloading OpenJDK 25 from %URL%
curl -L "%URL%" -o "%ARCHIVE%"

if errorlevel 1 (
    echo Error: Failed to download OpenJDK 25
    exit /b 1
)

echo [muisca] Removing existing JDK 25 directory...
if exist "%JDK_DIR%" rmdir /s /q "%JDK_DIR%"
mkdir "%JDK_DIR%"

echo [muisca] Extracting OpenJDK 25...
set EXTRACT_DIR=%ROOT_DIR%\.toolchains\extract_temp
if exist "%EXTRACT_DIR%" rmdir /s /q "%EXTRACT_DIR%"
mkdir "%EXTRACT_DIR%"

powershell -Command "Expand-Archive -Path '%ARCHIVE%' -DestinationPath '%EXTRACT_DIR%' -Force"

echo [muisca] Moving JDK to target directory...
set JDK_FOUND=0

rem Try to find the JDK directory in the extracted contents
if exist "%EXTRACT_DIR%\jdk" (
    xcopy /E /I /Y "%EXTRACT_DIR%\jdk\*" "%JDK_DIR%\" >nul
    set JDK_FOUND=1
)

if !JDK_FOUND!==0 (
    rem Look for directories starting with jdk
    for /d %%d in ("%EXTRACT_DIR%\jdk*") do (
        xcopy /E /I /Y "%%d\*" "%JDK_DIR%\" >nul
        set JDK_FOUND=1
        goto :check_found
    )
    :check_found
)

if !JDK_FOUND!==0 (
    rem Look for directories starting with openjdk
    for /d %%d in ("%EXTRACT_DIR%\openjdk*") do (
        xcopy /E /I /Y "%%d\*" "%JDK_DIR%\" >nul
        set JDK_FOUND=1
        goto :check_found2
    )
    :check_found2
)

if !JDK_FOUND!==0 (
    rem If still not found, copy everything from extract directory
    echo Warning: Could not find JDK directory structure, copying all contents...
    xcopy /E /I /Y "%EXTRACT_DIR%\*" "%JDK_DIR%\" >nul
)

rem Cleanup
rmdir /s /q "%EXTRACT_DIR%"

echo [muisca] JDK 25 extracted to %JDK_DIR%
echo.
echo To use Java 25, set the following environment variables:
echo set JAVA_HOME=%JDK_DIR%
echo set PATH=%%JAVA_HOME%%\bin;%%PATH%%
echo.
echo Or use Gradle toolchain (already configured in gradle.properties)

