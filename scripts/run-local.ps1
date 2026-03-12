# Run MicroPay locally with docker-compose.yml (includes config-server)
# Requires: Docker Desktop, .env in infrastructure/docker (copy from .env.example)
# Usage: .\scripts\run-local.ps1

$ErrorActionPreference = "Stop"
$dockerDir = Join-Path (Split-Path -Parent $PSScriptRoot) "infrastructure\docker"
$envFile = Join-Path $dockerDir ".env"

if (-not (Test-Path $envFile)) {
    Write-Host "[ERROR] Create infrastructure/docker/.env (copy from .env.example) with POSTGRES_PASSWORD set." -ForegroundColor Red
    exit 1
}

docker info 2>$null | Out-Null
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Docker is not running." -ForegroundColor Red
    exit 1
}

$compose = if (docker compose version 2>$null) { "docker compose" } else { "docker-compose" }
Set-Location $dockerDir
Write-Host "[INFO] Starting stack from $dockerDir ..." -ForegroundColor Green
Invoke-Expression "$compose --env-file .env up -d --build"
if ($LASTEXITCODE -ne 0) { exit 1 }
Write-Host "[INFO] Stack started. Frontend: http://localhost:3000  API Gateway: http://localhost:8080" -ForegroundColor Green
Write-Host "[INFO] Run .\scripts\verify-stack.ps1 after ~2 min to verify." -ForegroundColor Cyan
