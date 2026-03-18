# MicroPay Stack Startup Script (PowerShell)
# This script starts the entire MicroPay application stack

param(
    [string]$PostgresPassword,
    [string]$JwtSecret,
    [string]$PostgresUser = "postgres",
    [string]$PostgresDb = "postgres",
    [string]$SpringProfilesActive = "prod",
    [string]$KafkaBroker = "kafka:29092",
    [string]$JwtExpiration = "86400000",
    [string]$FrontendUrl = "http://127.0.0.1",
    [string]$ViteApiBaseUrl = "/api",
    [string]$EcrRegistry = "local",
    [string]$ImageTag = "latest",
    [string]$KafkaAdvertisedHost = "127.0.0.1"
)

# Colors for output
$Colors = @{
    Red = "Red"
    Green = "Green"
    Yellow = "Yellow"
    Blue = "Blue"
}

# Function to print colored output
function Write-Status([string]$Message) { Write-Host "[INFO] $Message" -ForegroundColor $Colors.Green }
function Write-Warning([string]$Message) { Write-Host "[WARN] $Message" -ForegroundColor $Colors.Yellow }
function Write-Error([string]$Message) { Write-Host "[ERROR] $Message" -ForegroundColor $Colors.Red }
function Write-Header([string]$Message) {
    Write-Host "========================================" -ForegroundColor $Colors.Blue
    Write-Host "$Message" -ForegroundColor $Colors.Blue
    Write-Host "========================================" -ForegroundColor $Colors.Blue
}

# Check if Docker is running
try {
    docker info 2>$null | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Docker not running" }
} catch {
    Write-Error "Docker is not running. Please start Docker first."
    exit 1
}

# Prefer Docker Compose V2 plugin, fallback to standalone
$dockerComposeCmd = "docker-compose"
try {
    docker compose version 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $dockerComposeCmd = "docker compose" }
} catch {
    # Stick with docker-compose
}

# Validate required parameters
if (-not $PostgresPassword) {
    Write-Error "POSTGRES_PASSWORD is required. Use -PostgresPassword parameter."
    exit 1
}

if (-not $JwtSecret) {
    Write-Error "JWT_SECRET is required. Use -JwtSecret parameter."
    exit 1
}

# Set environment variables
$env:POSTGRES_USER = $PostgresUser
$env:POSTGRES_PASSWORD = $PostgresPassword
$env:POSTGRES_DB = $PostgresDb
$env:SPRING_PROFILES_ACTIVE = $SpringProfilesActive
$env:KAFKA_BROKER = $KafkaBroker
$env:JWT_SECRET = $JwtSecret
$env:JWT_EXPIRATION = $JwtExpiration
$env:FRONTEND_URL = $FrontendUrl
$env:VITE_API_BASE_URL = $ViteApiBaseUrl

# Database URLs
$env:AUTH_DB_URL = "jdbc:postgresql://postgres:5432/micropay_auth_db"
$env:WALLET_DB_URL = "jdbc:postgresql://postgres:5432/micropay_wallet_db"
$env:PAYMENT_DB_URL = "jdbc:postgresql://postgres:5432/micropay_payment_db"
$env:TRANSACTION_DB_URL = "jdbc:postgresql://postgres:5432/micropay_transaction_db"
$env:NOTIFICATION_DB_URL = "jdbc:postgresql://postgres:5432/micropay_notification_db"

# Image settings
$env:ECR_REGISTRY = $EcrRegistry
$env:IMAGE_TAG = $ImageTag
$env:KAFKA_ADVERTISED_HOST = $KafkaAdvertisedHost

Write-Header "Starting MicroPay Stack with Docker Compose"

# Change to the correct directory
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$DockerDir = Join-Path $ScriptDir "..\infrastructure\docker"
Set-Location $DockerDir

Write-Status "Building and starting all services..."
Invoke-Expression "$dockerComposeCmd -f docker-compose.prod.yml up -d --build"

if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to start services. Please check Docker logs."
    exit 1
}

Write-Header "Waiting for services to be healthy..."

# Function to wait for service with timeout
function Wait-Service {
    param(
        [string]$ServiceName,
        [string]$HealthCheckCommand,
        [int]$TimeoutSeconds = 300
    )
    
    Write-Status "Waiting for $ServiceName..."
    $timeout = [DateTime]::Now.AddSeconds($TimeoutSeconds)
    
    while ([DateTime]::Now -lt $timeout) {
        try {
            Invoke-Expression $HealthCheckCommand 2>$null | Out-Null
            if ($LASTEXITCODE -eq 0) {
                Write-Status "$ServiceName is ready!"
                return $true
            }
        } catch {
            # Service not ready yet
        }
        Start-Sleep -Seconds 5
    }
    
    Write-Error "Timeout waiting for $ServiceName"
    return $false
}

if (-not (Wait-Service "PostgreSQL" "$dockerComposeCmd -f docker-compose.prod.yml exec -T postgres pg_isready -U $PostgresUser")) { exit 1 }
if (-not (Wait-Service "Zookeeper" "$dockerComposeCmd -f docker-compose.prod.yml exec -T zookeeper nc -z 127.0.0.1 2181")) { exit 1 }
if (-not (Wait-Service "Kafka" "$dockerComposeCmd -f docker-compose.prod.yml exec -T kafka kafka-topics --bootstrap-server 127.0.0.1:9092 --list")) { exit 1 }
if (-not (Wait-Service "Redis" "$dockerComposeCmd -f docker-compose.prod.yml exec -T redis redis-cli ping")) { exit 1 }

# Wait for application services
if (-not (Wait-Service "Eureka Server" "curl.exe -fs http://localhost:8761/actuator/health")) { exit 1 }
if (-not (Wait-Service "API Gateway" "curl.exe -fs http://localhost:8080/actuator/health")) { exit 1 }
if (-not (Wait-Service "Auth Service" "curl.exe -fs http://localhost:8081/actuator/health")) { exit 1 }
if (-not (Wait-Service "Wallet Service" "curl.exe -fs http://localhost:8083/actuator/health")) { exit 1 }
if (-not (Wait-Service "Payment Service" "curl.exe -fs http://localhost:8084/actuator/health")) { exit 1 }
if (-not (Wait-Service "Transaction Service" "curl.exe -fs http://localhost:8085/actuator/health")) { exit 1 }
if (-not (Wait-Service "Notification Service" "curl.exe -fs http://localhost:8086/actuator/health")) { exit 1 }

Write-Header "MicroPay Stack is Ready!"

Write-Host ""
Write-Status "Service URLs:"
Write-Host "  Frontend:            http://localhost"
Write-Host "  API Gateway:         http://localhost:8080"
Write-Host "  Auth Service:        http://localhost:8081"
Write-Host "  Wallet Service:      http://localhost:8083"
Write-Host "  Payment Service:     http://localhost:8084"
Write-Host "  Transaction Service: http://localhost:8085"
Write-Host "  Notification Service: http://localhost:8086"
Write-Host "  Eureka Server:       http://localhost:8761"
Write-Host ""
Write-Host "  Prometheus:          http://localhost:9090"
Write-Host "  Grafana:             http://localhost:3001 (admin/admin)"
Write-Host "  Kafka:               localhost:9092"
Write-Host ""

Write-Status "Health Check Commands:"
Write-Host "  $dockerComposeCmd -f docker-compose.prod.yml ps"
Write-Host "  $dockerComposeCmd -f docker-compose.prod.yml logs -f [service-name]"
Write-Host ""

Write-Status "To stop the stack:"
Write-Host "  $dockerComposeCmd -f docker-compose.prod.yml down"
Write-Host ""

Write-Warning "Make sure all required environment variables are set:"
Write-Host "  POSTGRES_PASSWORD, JWT_SECRET"
Write-Host ""

Write-Status "MicroPay stack started successfully!"