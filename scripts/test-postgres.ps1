param(
    [string]$PostgresBin = 'C:\Program Files\PostgreSQL\17\bin',
    [ValidateRange(1024, 65535)][int]$Port = 55432
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$cluster = Join-Path $projectRoot 'tools\postgres-test'
$log = Join-Path $projectRoot 'tools\postgres-test.log'
$databaseName = 'campusmarket_test_' + [Guid]::NewGuid().ToString('N').Substring(0, 12)
$initdb = Join-Path $PostgresBin 'initdb.exe'
$pgctl = Join-Path $PostgresBin 'pg_ctl.exe'
if (-not (Test-Path -LiteralPath $initdb)) { throw 'Informe a pasta bin de uma instalação PostgreSQL em -PostgresBin.' }
if (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue) { throw 'A porta de teste já está em uso. Escolha outra com -Port.' }
if (-not (Test-Path -LiteralPath (Join-Path $cluster 'PG_VERSION'))) {
    & $initdb -D $cluster -U campus_test -A trust --encoding UTF8 --no-locale
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível preparar o cluster de teste.' }
}
$previousUrl = $env:SPRING_DATASOURCE_URL
$previousUser = $env:SPRING_DATASOURCE_USERNAME
$previousPassword = $env:SPRING_DATASOURCE_PASSWORD
$started = $false
try {
    # Console oculto separado evita que o servidor herde os pipes do PowerShell.
    # Sem isso, redirecionar o log do script pode bloquear o retorno de pg_ctl.
    $startArguments = @('-D', ('"' + $cluster + '"'), '-l', ('"' + $log + '"'),
        '-o', ('"-h 127.0.0.1 -p ' + $Port + ' -F"'), '-w', 'start')
    $startProcess = Start-Process -FilePath $pgctl -ArgumentList $startArguments -WindowStyle Hidden -PassThru
    # Start-Process -Wait também espera o servidor descendente, que deve continuar
    # ativo. WaitForExit observa somente pg_ctl, que retorna quando o banco fica pronto.
    if (-not $startProcess.WaitForExit(60000)) { throw 'O PostgreSQL não confirmou a inicialização em 60 segundos.' }
    $startProcess.Refresh()
    if ($startProcess.ExitCode -ne 0) { throw 'Não foi possível iniciar o PostgreSQL de teste.' }
    $started = $true
    & (Join-Path $PostgresBin 'psql.exe') -h 127.0.0.1 -p $Port -U campus_test -d postgres -w -c "CREATE DATABASE $databaseName;"
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível criar o banco de teste.' }
    $env:SPRING_DATASOURCE_URL = "jdbc:postgresql://127.0.0.1:$Port/$databaseName"
    $env:SPRING_DATASOURCE_USERNAME = 'campus_test'
    $env:SPRING_DATASOURCE_PASSWORD = ''
    & (Join-Path $PSScriptRoot 'backend.ps1') test
    $testResult = $LASTEXITCODE
} finally {
    $env:SPRING_DATASOURCE_URL = $previousUrl
    $env:SPRING_DATASOURCE_USERNAME = $previousUser
    $env:SPRING_DATASOURCE_PASSWORD = $previousPassword
    if ($started) { & $pgctl -D $cluster -m fast -w stop }
}
exit $testResult
