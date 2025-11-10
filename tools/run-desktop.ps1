Param(
    [string]$Task = "desktop:run",
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

$prevJavaHome = $env:JAVA_HOME
$env:JAVA_HOME = $jdk25
$env:PATH = "{0};{1}" -f (Join-Path $jdk25 "bin"), $env:PATH

Write-Host "[muisca] JAVA_HOME => $env:JAVA_HOME"
Write-Host "[muisca] Launching gradlew task '$Task' with OpenJDK 21 as toolchain"

try {
    & (Join-Path $repoRoot "gradlew.bat") ("-Dorg.gradle.java.home={0}" -f $jdk21) $Task @GradleArgs
} finally {
    $env:JAVA_HOME = $prevJavaHome
}
