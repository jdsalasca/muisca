Param(
    [Parameter(Position = 0)]
    [string]$Task = "desktop:run",
    [switch]$GradleInfo,
    [switch]$GradleDebug,
    [switch]$ForceShapes,
    [switch]$UseAngle,
    [int]$AutoQuitSeconds = 0,
    [ValidateSet("21","25")]
    [string]$RuntimeJdk = "21",
    [string]$LogFile,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs = @()
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$toolchains = Join-Path $repoRoot ".toolchains"
$jdk21 = Join-Path $toolchains "openjdk21"
$jdk25 = Join-Path $toolchains "openjdk25"

function Test-AngleRequested {
    param([string[]]$Args)
    foreach ($a in $Args) {
        if ($a -match "muisca\.angle\s*=\s*true") { return $true }
    }
    return $false
}

function Find-AngleLibDir {
    # Returns a directory path that contains libEGL.dll AND libGLESv2.dll, or $null
    param()
    $candidates = @()
    # Manual override via environment or local folder
    if ($env:ANGLE_LIB_DIR) { $candidates += $env:ANGLE_LIB_DIR }
    $candidates += @(Join-Path $PSScriptRoot "angle"), (Join-Path $repoRoot "tools\angle"), (Join-Path $repoRoot "assets")
    # Common browser installs that ship ANGLE
    $chrome64 = "C:\Program Files\Google\Chrome\Application"
    $chrome32 = "C:\Program Files (x86)\Google\Chrome\Application"
    $edge64   = "C:\Program Files\Microsoft\Edge\Application"
    $edge32   = "C:\Program Files (x86)\Microsoft\Edge\Application"
    foreach ($base in @($chrome64,$chrome32,$edge64,$edge32)) {
        if (Test-Path $base) {
            # Search deepest versioned subfolder first for DLLs
            Get-ChildItem -Path $base -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | ForEach-Object {
                $candidates += $_.FullName
            }
            $candidates += $base
        }
    }
    foreach ($dir in $candidates) {
        try {
            if (-not $dir) { continue }
            $egl  = Join-Path $dir "libEGL.dll"
            $gles = Join-Path $dir "libGLESv2.dll"
            if ((Test-Path $egl) -and (Test-Path $gles)) { return $dir }
        } catch {}
    }
    return $null
}

function Ensure-Jdk {
    param(
        [string]$Name,
        [string]$InstallDir,
        [string]$Fetcher
    )

    if (Test-Path (Join-Path $InstallDir "bin\java.exe")) {
        Write-Host "[muisca] Found $Name at $InstallDir"
        return
    }

    Write-Host "[muisca] $Name not found, invoking $Fetcher"
    & $Fetcher | Write-Output

    if (-not (Test-Path (Join-Path $InstallDir "bin\java.exe"))) {
        throw "$Name setup failed, java.exe still missing at $InstallDir"
    }
}

Ensure-Jdk -Name "OpenJDK 21 (Gradle/runtime)" -InstallDir $jdk21 -Fetcher (Join-Path $PSScriptRoot "fetch-openjdk21.bat")
Ensure-Jdk -Name "OpenJDK 25 (opcional)" -InstallDir $jdk25 -Fetcher (Join-Path $PSScriptRoot "fetch-openjdk25.bat")

$Task = if ([string]::IsNullOrWhiteSpace($Task)) { "desktop:run" } else { $Task }

$prevJavaHome = $env:JAVA_HOME
if ($RuntimeJdk -eq "21") {
    $env:JAVA_HOME = $jdk21
    $env:PATH = "{0};{1}" -f (Join-Path $jdk21 "bin"), $env:PATH
} else {
    $env:JAVA_HOME = $jdk25
    $env:PATH = "{0};{1}" -f (Join-Path $jdk25 "bin"), $env:PATH
}

$gradleSwitches = @()
if ($GradleInfo) { $gradleSwitches += "--info" }
if ($GradleDebug) { $gradleSwitches += "--debug" }

# Inject helper system props before passing everything to Gradle
if ($ForceShapes -and -not ($GradleArgs -match "muisca\.forceShapes\s*=\s*true")) {
    $GradleArgs = @("-Dmuisca.forceShapes=true") + $GradleArgs
    Write-Host "[muisca] ForceShapes activo (-Dmuisca.forceShapes=true)"
}
if ($UseAngle -and -not (Test-AngleRequested -Args $GradleArgs)) {
    $GradleArgs = @("-Dmuisca.angle=true") + $GradleArgs
    Write-Host "[muisca] ANGLE activado (-Dmuisca.angle=true)"
}

$logDir = Join-Path $repoRoot "logs"
if (-not $LogFile) {
    if (-not (Test-Path $logDir)) {
        New-Item -ItemType Directory -Path $logDir | Out-Null
    }
    $stamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $LogFile = Join-Path $logDir ("run-desktop-{0}.log" -f $stamp)
} else {
    $parent = Split-Path -Parent $LogFile
    if ($parent -and (Test-Path $parent) -eq $false) {
        New-Item -ItemType Directory -Path $parent | Out-Null
    }
}
$null = New-Item -ItemType File -Path $LogFile -Force

$gradleExecutable = Join-Path $repoRoot "gradlew.bat"
# Por defecto Gradle corre con JDK 21 para evitar incompatibilidades en LWJGL
$gradleArguments = @("-Dorg.gradle.java.home={0}" -f $jdk21)
if ($AutoQuitSeconds -gt 0) {
    $gradleArguments += ("-Dmuisca.autoQuitSeconds={0}" -f $AutoQuitSeconds)
}
# Place Gradle system properties before the task name to ensure Gradle picks them up
$gradleArguments += $gradleSwitches + $GradleArgs + @($Task)

# If user requested ANGLE (muisca.angle=true), attempt to locate ANGLE DLLs and add to PATH
if (Test-AngleRequested -Args $GradleArgs) {
    $angleDir = Find-AngleLibDir
    if ($angleDir) {
        Write-Host "[muisca] ANGLE runtime found => $angleDir"
        $env:PATH = "$angleDir;" + $env:PATH
    } else {
        Write-Warning "[muisca] ANGLE requested but libEGL.dll/libGLESv2.dll not found in common locations."
        Write-Warning "          Please install Chrome/Edge or place the DLLs under tools/angle (or set ANGLE_LIB_DIR)."
    }
}

function Format-Arg {
    param([string]$Value)
    if ([string]::IsNullOrEmpty($Value)) { return $Value }
    if ($Value -match '\s') {
        return '"{0}"' -f $Value
    }
    return $Value
}

$commandPreview = "$gradleExecutable " + (($gradleArguments | ForEach-Object { Format-Arg $_ }) -join ' ')

Write-Host "[muisca] JAVA_HOME (runtime) => $env:JAVA_HOME"
Write-Host "[muisca] Log file => $LogFile"
Write-Host "[muisca] Running: $commandPreview"

$previousErrorPreference = $ErrorActionPreference
try {
    $ErrorActionPreference = "Continue"
    & $gradleExecutable @gradleArguments 2>&1 | Tee-Object -FilePath $LogFile
    $gradleExitCode = $LASTEXITCODE
} finally {
    $ErrorActionPreference = $previousErrorPreference
    $env:JAVA_HOME = $prevJavaHome
}

if ($gradleExitCode -ne 0) {
    throw "Gradle exited with code $gradleExitCode. Revisa $LogFile para más detalles."
}

Write-Host "[muisca] Task '$Task' finished. Logs guardados en $LogFile"
