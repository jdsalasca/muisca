Param(
    [Parameter(Position = 0)]
    [string]$Task = "desktop:run",
    [switch]$GradleInfo,
    [switch]$GradleDebug,
    [int]$AutoQuitSeconds = 0,
    [ValidateSet("21","25")]
    [string]$RuntimeJdk = "25",
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

Ensure-Jdk -Name "OpenJDK 21 (Gradle)" -InstallDir $jdk21 -Fetcher (Join-Path $PSScriptRoot "fetch-openjdk21.bat")
Ensure-Jdk -Name "OpenJDK 25 (runtime)" -InstallDir $jdk25 -Fetcher (Join-Path $PSScriptRoot "fetch-openjdk25.bat")

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
$gradleArguments = @("-Dorg.gradle.java.home={0}" -f $jdk21)
if ($AutoQuitSeconds -gt 0) {
    $gradleArguments += ("-Dmuisca.autoQuitSeconds={0}" -f $AutoQuitSeconds)
}
$gradleArguments += $gradleSwitches + @($Task) + $GradleArgs

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
