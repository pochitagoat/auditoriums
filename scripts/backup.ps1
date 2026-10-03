param(
    [string]$OutputDirectory = (Join-Path 'backups' (Get-Date -Format 'yyyyMMdd-HHmmss')),
    [string]$ProjectName
)
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$originalBackupDirectory = $env:BACKUP_DIRECTORY
$composeArguments = @('compose')
if ($ProjectName) { $composeArguments += @('--project-name', $ProjectName) }
function Invoke-Compose {
    & docker @composeArguments @args
    if ($LASTEXITCODE -ne 0) { throw 'Команда Docker Compose завершилась с ошибкой' }
}
$appWasRunning = $false
try {
    $destination = [System.IO.Path]::GetFullPath($(if ([System.IO.Path]::IsPathRooted($OutputDirectory)) { $OutputDirectory } else { Join-Path (Get-Location).Path $OutputDirectory }))
    if ((Test-Path -LiteralPath (Join-Path $destination 'database.dump')) -or
        (Test-Path -LiteralPath (Join-Path $destination 'photos.tar.gz'))) { throw 'В каталоге уже есть резервная копия' }
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    $env:BACKUP_DIRECTORY = $destination
    $appWasRunning = @(Invoke-Compose ps --status running --services) -contains 'app'
    if ($appWasRunning) { Invoke-Compose stop app }
    # Запись dump внутри контейнера и compose cp сохраняют бинарные байты в PowerShell.
    Invoke-Compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc -f /tmp/auditoriums-backup.dump'
    Invoke-Compose cp postgres:/tmp/auditoriums-backup.dump (Join-Path $destination 'database.dump')
    Invoke-Compose run --rm -T files sh -c 'tar -czf /backup/photos.tar.gz -C /photos .'
    $manifest = @{
        createdAtUtc = [DateTime]::UtcNow.ToString('o')
        postgresMajor = 17
        photoRoot = '/app/photos'
        databaseSha256 = (Get-FileHash -LiteralPath (Join-Path $destination 'database.dump') -Algorithm SHA256).Hash
        photosSha256 = (Get-FileHash -LiteralPath (Join-Path $destination 'photos.tar.gz') -Algorithm SHA256).Hash
    }
    $manifest | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $destination 'manifest.json') -Encoding utf8
    Write-Host "Резервная копия создана: $destination"
} finally {
    try { if ($appWasRunning) { Invoke-Compose up -d --wait app } }
    finally { $env:BACKUP_DIRECTORY = $originalBackupDirectory; Pop-Location }
}
