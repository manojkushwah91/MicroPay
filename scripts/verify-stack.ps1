# MicroPay Stack Verification Script
# Run after: docker compose up -d (from infrastructure/docker)
# Usage: .\scripts\verify-stack.ps1

$ErrorActionPreference = "Stop"
$base = "http://localhost"
$gateway = "http://localhost:8080"
$eureka = "http://localhost:8761"

function Test-Endpoint {
    param([string]$Name, [string]$Url, [string]$Method = "GET", [hashtable]$Headers = @{})
    try {
        $params = @{ Uri = $Url; Method = $Method; UseBasicParsing = $true; TimeoutSec = 5 }
        if ($Headers.Count -gt 0) { $params.Headers = $Headers }
        $r = Invoke-WebRequest @params
        if ($r.StatusCode -ge 200 -and $r.StatusCode -lt 300) {
            Write-Host "  [OK] $Name" -ForegroundColor Green
            return $true
        }
    } catch {
        Write-Host "  [FAIL] $Name - $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
    Write-Host "  [FAIL] $Name" -ForegroundColor Red
    return $false
}

Write-Host "`n=== MicroPay Stack Verification ===" -ForegroundColor Cyan
Write-Host ""

# Infrastructure
Write-Host "Infrastructure:" -ForegroundColor Yellow
$ok = $true
$ok = (Test-Endpoint "Eureka" "$eureka/actuator/health") -and $ok
$ok = (Test-Endpoint "API Gateway" "$gateway/actuator/health") -and $ok
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8888/actuator/health" -UseBasicParsing -TimeoutSec 3
    if ($r.StatusCode -eq 200) { Write-Host "  [OK] Config Server" -ForegroundColor Green }
    else { Write-Host "  [SKIP] Config Server (optional)" -ForegroundColor Gray }
} catch {
    Write-Host "  [SKIP] Config Server (optional)" -ForegroundColor Gray
}

# Services (via Gateway or direct)
Write-Host "`nServices (direct health):" -ForegroundColor Yellow
$ok = (Test-Endpoint "Auth Service" "http://localhost:8081/actuator/health") -and $ok
$ok = (Test-Endpoint "Wallet Service" "http://localhost:8083/actuator/health") -and $ok
$ok = (Test-Endpoint "Payment Service" "http://localhost:8084/actuator/health") -and $ok
$ok = (Test-Endpoint "Transaction Service" "http://localhost:8085/actuator/health") -and $ok
$ok = (Test-Endpoint "Notification Service" "http://localhost:8086/actuator/health") -and $ok
$ok = (Test-Endpoint "User Service" "http://localhost:8082/actuator/health") -and $ok

# Gateway routing (public auth - no token)
Write-Host "`nGateway routing:" -ForegroundColor Yellow
$body = '{"email":"verify@test.com","password":"pass123","firstName":"V","lastName":"T"}'
try {
    $r = Invoke-RestMethod -Uri "$gateway/api/auth/register" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    if ($r.token -and $r.userId) {
        Write-Host "  [OK] POST /api/auth/register (via Gateway)" -ForegroundColor Green
        $token = $r.token
        $userId = $r.userId
    } else { Write-Host "  [FAIL] POST /api/auth/register - no token/userId" -ForegroundColor Red; $ok = $false }
} catch {
    Write-Host "  [FAIL] POST /api/auth/register - $($_.Exception.Message)" -ForegroundColor Red
    $ok = $false
}

if ($token) {
    $headers = @{ Authorization = "Bearer $token" }
    $ok = (Test-Endpoint "GET /api/wallet (authenticated)" "$gateway/api/wallet/$userId" -Headers $headers) -and $ok
}

Write-Host ""
if ($ok) {
    Write-Host "All checks passed. Stack is ready." -ForegroundColor Green
} else {
    Write-Host "Some checks failed. Review logs: docker compose logs -f" -ForegroundColor Red
    exit 1
}
Write-Host ""
