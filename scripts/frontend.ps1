param([ValidateSet('dev', 'build', 'install')][string]$Action = 'dev')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$store = Join-Path $projectRoot 'tools\pnpm-store'
$pnpm = Get-Command pnpm.cmd -ErrorAction SilentlyContinue
$npm = Get-Command npm.cmd -ErrorAction SilentlyContinue
$bundledPnpm = Join-Path $env:USERPROFILE '.cache\codex-runtimes\codex-primary-runtime\dependencies\node\node_modules\pnpm\bin\pnpm.cjs'
Push-Location (Join-Path $projectRoot 'frontend')
try {
    if ($Action -ne 'install') {
        # Executa as ferramentas locais diretamente. A instalação é uma etapa
        # explícita, evitando reinstalações automáticas do pnpm ao iniciar a tela.
        if (-not (Test-Path -LiteralPath '.\node_modules\vite\bin\vite.js')) {
            throw 'Execute primeiro .\scripts\frontend.ps1 install na raiz do projeto.'
        }
        if ($Action -eq 'build') {
            & node '.\node_modules\typescript\bin\tsc' -b
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
            & node '.\node_modules\vite\bin\vite.js' build
        } else {
            & node '.\node_modules\vite\bin\vite.js' --host 127.0.0.1
        }
    } elseif ($pnpm) {
        & $pnpm.Source install --store-dir $store
    } elseif (Test-Path -LiteralPath $bundledPnpm) {
        & node $bundledPnpm install --store-dir $store
    } elseif ($npm) {
        & $npm.Source install
    } else { throw 'Instale o Node.js com npm ou pnpm para instalar as dependências.' }
    $result = $LASTEXITCODE
} finally { Pop-Location }
exit $result
