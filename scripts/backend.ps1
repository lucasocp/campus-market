param(
    [ValidateSet('run', 'test', 'compile', 'package')]
    [string]$Action = 'run',
    [string]$Profile = 'demo'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot

# Usa o ambiente existente ou um JDK local, sem gravar variáveis do Windows.
$javaCandidates = @($env:JAVA_HOME)
$portableJdks = Get-ChildItem -LiteralPath (Join-Path $projectRoot 'tools') -Directory -ErrorAction SilentlyContinue |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'bin\javac.exe') }
$javaCandidates += @($portableJdks | ForEach-Object { $_.FullName })
$javaCandidates += 'C:\Program Files\Android\Android Studio\jbr'
$selectedJava = $javaCandidates | Where-Object { $_ -and (Test-Path -LiteralPath (Join-Path $_ 'bin\javac.exe')) } | Select-Object -First 1
if ($selectedJava) { $env:JAVA_HOME = $selectedJava }
elseif (-not (Get-Command javac -ErrorAction SilentlyContinue)) { throw 'Instale um JDK 21 ou superior e configure JAVA_HOME.' }

$maven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
if ($maven) { $mavenPath = $maven.Source }
else {
    $mavenPath = Join-Path $projectRoot 'tools\apache-maven-3.9.16\bin\mvn.cmd'
    if (-not (Test-Path -LiteralPath $mavenPath)) { throw 'Instale o Maven 3.9+ ou extraia a versão 3.9.16 em tools.' }
}
$cache = Join-Path $projectRoot 'tools\m2'
$mavenArguments = @('-B', "-Dmaven.repo.local=$cache")
switch ($Action) {
    'run' { $mavenArguments += @('spring-boot:run', "-Dspring-boot.run.profiles=$Profile") }
    'test' { $mavenArguments += 'test' }
    'compile' { $mavenArguments += 'compile' }
    'package' { $mavenArguments += 'package' }
}
Push-Location (Join-Path $projectRoot 'backend')
try { & $mavenPath @mavenArguments; $result = $LASTEXITCODE }
finally { Pop-Location }
exit $result
