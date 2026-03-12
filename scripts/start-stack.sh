#!/bin/bash

# MicroPay Stack Startup Script
# This script starts the entire MicroPay application stack

set -e

echo "🚀 Starting MicroPay Application Stack..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose > /dev/null 2>&1; then
    print_error "docker-compose is not installed. Please install docker-compose first."
    exit 1
fi

# Set environment variables
export POSTGRES_USER=${POSTGRES_USER:-postgres}
export POSTGRES_PASSWORD=${POSTGRES_PASSWORD:?POSTGRES_PASSWORD is required}
export POSTGRES_DB=${POSTGRES_DB:-postgres}
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}
export KAFKA_BROKER=${KAFKA_BROKER:-kafka:29092}
export JWT_SECRET=${JWT_SECRET:?JWT_SECRET is required}
export JWT_EXPIRATION=${JWT_EXPIRATION:-86400000}
export FRONTEND_URL=${FRONTEND_URL:-http://127.0.0.1}
export VITE_API_BASE_URL=${VITE_API_BASE_URL:-/api}

# Database URLs
export AUTH_DB_URL=${AUTH_DB_URL:-jdbc:postgresql://postgres:5432/micropay_auth_db}
export WALLET_DB_URL=${WALLET_DB_URL:-jdbc:postgresql://postgres:5432/micropay_wallet_db}
export PAYMENT_DB_URL=${PAYMENT_DB_URL:-jdbc:postgresql://postgres:5432/micropay_payment_db}
export TRANSACTION_DB_URL=${TRANSACTION_DB_URL:-jdbc:postgresql://postgres:5432/micropay_transaction_db}
export NOTIFICATION_DB_URL=${NOTIFICATION_DB_URL:-jdbc:postgresql://postgres:5432/micropay_notification_db}

# Image settings
export ECR_REGISTRY=${ECR_REGISTRY:-local}
export IMAGE_TAG=${IMAGE_TAG:-latest}
export KAFKA_ADVERTISED_HOST=${KAFKA_ADVERTISED_HOST:-127.0.0.1}

print_header "Starting MicroPay Stack with Docker Compose"

# Change to the correct directory
cd "$(dirname "$0")/../infrastructure/docker"

print_status "Building and starting all services..."
docker-compose -f docker-compose.prod.yml up -d --build

print_header "Waiting for services to be healthy..."

# Wait for infrastructure services to be healthy
print_status "Waiting for PostgreSQL..."
timeout 300 bash -c 'until docker-compose -f docker-compose.prod.yml exec -T postgres pg_isready -U ${POSTGRES_USER:-postgres} > /dev/null 2>&1; do sleep 2; done'

print_status "Waiting for Zookeeper..."
timeout 300 bash -c 'until docker-compose -f docker-compose.prod.yml exec -T zookeeper nc -z 127.0.0.1 2181 > /dev/null 2>&1; do sleep 2; done'

print_status "Waiting for Kafka..."
timeout 300 bash -c 'until docker-compose -f docker-compose.prod.yml exec -T kafka kafka-topics --bootstrap-server 127.0.0.1:9092 --list > /dev/null 2>&1; do sleep 2; done'

print_status "Waiting for Redis..."
timeout 300 bash -c 'until docker-compose -f docker-compose.prod.yml exec -T redis redis-cli ping > /dev/null 2>&1; do sleep 2; done'

# Wait for application services
print_status "Waiting for Eureka Server..."
timeout 300 bash -c 'until curl -fs http://localhost:8761/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_status "Waiting for API Gateway..."
timeout 300 bash -c 'until curl -fs http://localhost:8080/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_status "Waiting for Auth Service..."
timeout 300 bash -c 'until curl -fs http://localhost:8081/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_status "Waiting for Wallet Service..."
timeout 300 bash -c 'until curl -fs http://localhost:8083/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_status "Waiting for Payment Service..."
timeout 300 bash -c 'until curl -fs http://localhost:8084/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_status "Waiting for Transaction Service..."
timeout 300 bash -c 'until curl -fs http://localhost:8085/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_status "Waiting for Notification Service..."
timeout 300 bash -c 'until curl -fs http://localhost:8086/actuator/health > /dev/null 2>&1; do sleep 5; done'

print_header "MicroPay Stack is Ready! 🎉"

echo ""
print_status "Service URLs:"
echo "  🌐 Frontend:            http://localhost"
echo "  🚪 API Gateway:         http://localhost:8080"
echo "  🔐 Auth Service:        http://localhost:8081"
echo "  💳 Wallet Service:      http://localhost:8083"
echo "  💰 Payment Service:     http://localhost:8084"
echo "  📊 Transaction Service: http://localhost:8085"
echo "  📧 Notification Service: http://localhost:8086"
echo "  🔍 Eureka Server:       http://localhost:8761"
echo ""
echo "  📈 Prometheus:          http://localhost:9090"
echo "  📊 Grafana:             http://localhost:3001 (admin/admin)"
echo "  📨 Kafka:               localhost:9092"
echo ""

print_status "Health Check Commands:"
echo "  docker-compose -f docker-compose.prod.yml ps"
echo "  docker-compose -f docker-compose.prod.yml logs -f [service-name]"
echo ""

print_status "To stop the stack:"
echo "  docker-compose -f docker-compose.prod.yml down"
echo ""

print_status "To view logs:"
echo "  docker-compose -f docker-compose.prod.yml logs -f"
echo ""

print_warning "Make sure all required environment variables are set:"
echo "  POSTGRES_PASSWORD, JWT_SECRET"
echo ""

echo "✅ MicroPay stack started successfully!"
