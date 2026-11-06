$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$outputDirectory = Join-Path $projectRoot 'artifacts'
New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
$archive = Join-Path $outputDirectory 'campus-market-fonte.zip'
if (Test-Path -LiteralPath $archive) {
    $archive = Join-Path $outputDirectory ('campus-market-fonte-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.zip')
}

# Lista explícita: não inclui ferramentas, caches, bancos, .env ou node_modules.
$sourceDirectories = @('backend\src', 'frontend\src', 'frontend\public', 'docs', 'scripts')
$sourceFiles = @('README.md', '.gitignore', '.env.example', 'compose.yml', 'backend\pom.xml',
    'frontend\package.json', 'frontend\pnpm-lock.yaml', 'frontend\pnpm-workspace.yaml',
    'frontend\index.html', 'frontend\tsconfig.json', 'frontend\vite.config.ts')
$files = @()
foreach ($directory in $sourceDirectories) {
    $files += Get-ChildItem -LiteralPath (Join-Path $projectRoot $directory) -File -Recurse
}
foreach ($relative in $sourceFiles) {
    $path = Join-Path $projectRoot $relative
    if (Test-Path -LiteralPath $path -PathType Leaf) { $files += Get-Item -LiteralPath $path }
}
$zip = [System.IO.Compression.ZipFile]::Open($archive, [System.IO.Compression.ZipArchiveMode]::Create)
try {
    foreach ($file in $files) {
        $relative = [System.IO.Path]::GetRelativePath($projectRoot, $file.FullName).Replace('\', '/')
        [System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $file.FullName,
            'campus-market/' + $relative, [System.IO.Compression.CompressionLevel]::Optimal) | Out-Null
    }
} finally { $zip.Dispose() }
Write-Output ('Código-fonte empacotado: ' + $archive)
Write-Output ('Arquivos incluídos: ' + $files.Count)
