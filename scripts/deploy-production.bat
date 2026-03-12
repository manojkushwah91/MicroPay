@echo off
REM MicroPay Production Deployment Script for Windows
REM This script starts the entire MicroPay stack in production mode

echo 🚀 Starting MicroPay Production Deployment...

REM Check if .env file exists
if not exist ".env" (
    echo ❌ Error: .env file not found. Please copy .env.example to .env and configure it.
    exit /b 1
)

echo 📦 Building and starting services...

REM Start the production stack
docker-compose -f infrastructure/docker/docker-compose.prod.yml up -d

echo ⏳ Waiting for services to be ready...

echo 🔍 Checking service health...

REM Function to check service health (simplified for Windows)
echo.
echo 🏥 Verifying critical service health endpoints...
echo.

REM Wait a bit for services to start
timeout /t 60 /nobreak >nul

REM Check application services
echo Checking Eureka Server...
curl -f -s http://localhost:8761/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Eureka Server is healthy
) else (
    echo ❌ Eureka Server failed to become healthy
)

echo Checking API Gateway...
curl -f -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ API Gateway is healthy
) else (
    echo ❌ API Gateway failed to become healthy
)

echo Checking Auth Service...
curl -f -s http://localhost:8081/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Auth Service is healthy
) else (
    echo ❌ Auth Service failed to become healthy
)

echo Checking Wallet Service...
curl -f -s http://localhost:8083/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Wallet Service is healthy
) else (
    echo ❌ Wallet Service failed to become healthy
)

echo Checking Payment Service...
curl -f -s http://localhost:8084/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Payment Service is healthy
) else (
    echo ❌ Payment Service failed to become healthy
)

echo Checking Transaction Service...
curl -f -s http://localhost:8085/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Transaction Service is healthy
) else (
    echo ❌ Transaction Service failed to become healthy
)

echo Checking Notification Service...
curl -f -s http://localhost:8086/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Notification Service is healthy
) else (
    echo ❌ Notification Service failed to become healthy
)

echo.
echo 🎉 MicroPay Production Deployment Complete!
echo.
echo 📊 Service URLs:
echo   🌐 Frontend: http://localhost
echo   🚪 API Gateway: http://localhost:8080
echo   🔐 Auth Service: http://localhost:8081
echo   💰 Wallet Service: http://localhost:8083
echo   💳 Payment Service: http://localhost:8084
echo   📈 Transaction Service: http://localhost:8085
echo   📬 Notification Service: http://localhost:8086
echo   🗺️  Eureka Server: http://localhost:8761
echo   📊 Prometheus: http://localhost:9090
echo   📈 Grafana: http://localhost:3001 (admin/admin)
echo.
echo 🔧 To view logs: docker-compose -f infrastructure/docker/docker-compose.prod.yml logs -f [service-name]
echo 🛑 To stop: docker-compose -f infrastructure/docker/docker-compose.prod.yml down
echo.
pause
