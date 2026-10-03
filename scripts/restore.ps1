param(
    [Parameter(Mandatory = $true)][string]$BackupDirectory,
    [Parameter(Mandatory = $true)][ValidatePattern('^[a-z][a-z0-9_-]*$')][string]$ProjectName,
    [int]$AppPort = 8081,
    [int]$PostgresPort = 5434
)
$ErrorActionPreference = 'Stop'
Push-Location -LiteralPath (Split-Path -Parent $PSScriptRoot)
$originalBackupDirectory = $env:BACKUP_DIRECTORY
$originalAppPort = $env:APP_PORT
$originalPostgresPort = $env:POSTGRES_PORT
$originalProfile = $env:APP_PROFILE
$composeArguments = @('compose', '--project-name', $ProjectName)
function Invoke-Compose {
    & docker @composeArguments @args
    if ($LASTEXITCODE -ne 0) { throw 'Команда Docker Compose завершилась с ошибкой' }
}
try {
    $source = (Resolve-Path -LiteralPath $BackupDirectory).Path
    $manifest = Get-Content -LiteralPath (Join-Path $source 'manifest.json') -Raw | ConvertFrom-Json
    if ($manifest.postgresMajor -ne 17) { throw 'Эта копия требует другой major-версии PostgreSQL' }
    if ((Get-FileHash -LiteralPath (Join-Path $source 'database.dump')).Hash -ne $manifest.databaseSha256 -or
        (Get-FileHash -LiteralPath (Join-Path $source 'photos.tar.gz')).Hash -ne $manifest.photosSha256) { throw 'Контрольная сумма резервной копии не совпадает' }
    $existingVolumes = @(& docker volume ls --filter "label=com.docker.compose.project=$ProjectName" --format '{{.Name}}')
    if ($LASTEXITCODE -ne 0) { throw 'Не удалось проверить существующие volumes' }
    if (@(Invoke-Compose ps --all --quiet).Count -gt 0 -or $existingVolumes.Count -gt 0) {
        throw 'Восстановление разрешено только в новый Compose-проект без контейнеров и volumes'
    }
    $env:BACKUP_DIRECTORY = $source
    $env:APP_PORT = $AppPort.ToString()
    $env:POSTGRES_PORT = $PostgresPort.ToString()
    $env:APP_PROFILE = 'default'
    Invoke-Compose up -d --wait postgres
    Invoke-Compose cp (Join-Path $source 'database.dump') postgres:/tmp/auditoriums-restore.dump
    Invoke-Compose exec -T postgres sh -c 'pg_restore -U "$POSTGRES_USER" -d "$POSTGRES_DB" --no-owner --no-privileges --exit-on-error /tmp/auditoriums-restore.dump'
    Invoke-Compose run --rm -T files sh -c 'tar -xzf /backup/photos.tar.gz -C /photos && chown -R 10001:10001 /photos'
    Invoke-Compose up -d --wait app
    Write-Host "Восстановление выполнено в отдельное окружение: http://localhost:$AppPort"
} finally {
    $env:BACKUP_DIRECTORY = $originalBackupDirectory
    $env:APP_PORT = $originalAppPort
    $env:POSTGRES_PORT = $originalPostgresPort
    $env:APP_PROFILE = $originalProfile
    Pop-Location
}
