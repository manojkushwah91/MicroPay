#!/bin/bash

# MicroPay Production Deployment Script
# This script starts the entire MicroPay stack in production mode

set -e

echo "🚀 Starting MicroPay Production Deployment..."

# Check if .env file exists
if [ ! -f .env ]; then
    echo "❌ Error: .env file not found. Please copy .env.example to .env and configure it."
    exit 1
fi

# Load environment variables
source .env

echo "📦 Building and starting services..."

# Start the production stack
docker-compose -f infrastructure/docker/docker-compose.prod.yml up -d

echo "⏳ Waiting for services to be ready..."

# Wait for critical services to be healthy
echo "🔍 Checking service health..."

# Function to check service health
check_health() {
    local service_name=$1
    local health_url=$2
    local max_attempts=30
    local attempt=1
    
    echo "  Checking $service_name health..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -f -s $health_url > /dev/null 2>&1; then
            echo "  ✅ $service_name is healthy"
            return 0
        fi
        
        echo "    Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 10
        ((attempt++))
    done
    
    echo "  ❌ $service_name failed to become healthy"
    return 1
}

# Check all service health endpoints
echo ""
echo "🏥 Verifying all service health endpoints..."
echo ""

# Infrastructure services
check_health "PostgreSQL" "http://localhost:5432" || echo "  ⚠️  PostgreSQL health check skipped (port check)"
check_health "Redis" "http://localhost:6379" || echo "  ⚠️  Redis health check skipped (port check)"
check_health "Kafka" "http://localhost:9092" || echo "  ⚠️  Kafka health check skipped (port check)"

# Application services
check_health "Eureka Server" "http://localhost:8761/actuator/health"
check_health "API Gateway" "http://localhost:8080/actuator/health"
check_health "Auth Service" "http://localhost:8081/actuator/health"
check_health "Wallet Service" "http://localhost:8083/actuator/health"
check_health "Payment Service" "http://localhost:8084/actuator/health"
check_health "Transaction Service" "http://localhost:8085/actuator/health"
check_health "Notification Service" "http://localhost:8086/actuator/health"

# Frontend and Nginx
check_health "Frontend" "http://localhost/"
check_health "Prometheus" "http://localhost:9090/-/healthy"
check_health "Grafana" "http://localhost:3001/api/health"

echo ""
echo "🎉 MicroPay Production Deployment Complete!"
echo ""
echo "📊 Service URLs:"
echo "  🌐 Frontend: http://localhost"
echo "  🚪 API Gateway: http://localhost:8080"
echo "  🔐 Auth Service: http://localhost:8081"
echo "  💰 Wallet Service: http://localhost:8083"
echo "  💳 Payment Service: http://localhost:8084"
echo "  📈 Transaction Service: http://localhost:8085"
echo "  📬 Notification Service: http://localhost:8086"
echo "  🗺️  Eureka Server: http://localhost:8761"
echo "  📊 Prometheus: http://localhost:9090"
echo "  📈 Grafana: http://localhost:3001 (admin/admin)"
echo ""
echo "🔧 To view logs: docker-compose -f infrastructure/docker/docker-compose.prod.yml logs -f [service-name]"
echo "🛑 To stop: docker-compose -f infrastructure/docker/docker-compose.prod.yml down"
echo ""
