# MicroPay Stack Startup Scripts

This directory contains scripts to start the entire MicroPay application stack with a single command.

## Available Scripts

### 1. start-stack.sh (Linux/macOS)
Bash script for Linux and macOS systems.

### 2. start-stack.ps1 (Windows PowerShell)
PowerShell script for Windows systems.

## Prerequisites

- Docker and Docker Compose installed
- All required environment variables set

## Usage

### Windows (PowerShell)

```powershell
# Navigate to the scripts directory
cd scripts

# Run the startup script with required parameters
.\start-stack.ps1 -PostgresPassword "your-password" -JwtSecret "your-jwt-secret"

# Optional parameters (with defaults shown)
.\start-stack.ps1 -PostgresPassword "your-password" -JwtSecret "your-jwt-secret" `
    -PostgresUser "postgres" `
    -PostgresDb "postgres" `
    -SpringProfilesActive "prod" `
    -KafkaBroker "kafka:29092" `
    -JwtExpiration "86400000" `
    -FrontendUrl "http://127.0.0.1" `
    -ViteApiBaseUrl "/api" `
    -EcrRegistry "local" `
    -ImageTag "latest" `
    -KafkaAdvertisedHost "127.0.0.1"
```

### Linux/macOS (Bash)

```bash
# Navigate to the scripts directory
cd scripts

# Export required environment variables
export POSTGRES_PASSWORD="your-password"
export JWT_SECRET="your-jwt-secret"

# Make the script executable
chmod +x start-stack.sh

# Run the startup script
./start-stack.sh

# Or with environment variables inline
POSTGRES_PASSWORD="your-password" JWT_SECRET="your-jwt-secret" ./start-stack.sh
```

## Required Environment Variables

- `POSTGRES_PASSWORD`: PostgreSQL database password (required)
- `JWT_SECRET`: JWT secret key for authentication (required)

## Optional Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `POSTGRES_USER` | postgres | PostgreSQL username |
| `POSTGRES_DB` | postgres | Default database name |
| `SPRING_PROFILES_ACTIVE` | prod | Spring profile to use |
| `KAFKA_BROKER` | kafka:29092 | Kafka bootstrap servers |
| `JWT_EXPIRATION` | 86400000 | JWT token expiration time (ms) |
| `FRONTEND_URL` | http://127.0.0.1 | Frontend application URL |
| `VITE_API_BASE_URL` | /api | API base URL for frontend |
| `ECR_REGISTRY` | local | Docker registry for images |
| `IMAGE_TAG` | latest | Docker image tag |
| `KAFKA_ADVERTISED_HOST` | 127.0.0.1 | Kafka advertised host |

## Service URLs

Once the stack is started, you can access the services at:

- **Frontend**: http://localhost
- **API Gateway**: http://localhost:8080
- **Auth Service**: http://localhost:8081
- **Wallet Service**: http://localhost:8083
- **Payment Service**: http://localhost:8084
- **Transaction Service**: http://localhost:8085
- **Notification Service**: http://localhost:8086
- **Eureka Server**: http://localhost:8761
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3001 (admin/admin)

## Management Commands

### Check Service Status
```bash
docker-compose -f infrastructure/docker/docker-compose.prod.yml ps
```

### View Logs
```bash
# View all logs
docker-compose -f infrastructure/docker/docker-compose.prod.yml logs -f

# View specific service logs
docker-compose -f infrastructure/docker/docker-compose.prod.yml logs -f [service-name]
```

### Stop the Stack
```bash
docker-compose -f infrastructure/docker/docker-compose.prod.yml down
```

### Restart Services
```bash
docker-compose -f infrastructure/docker/docker-compose.prod.yml restart [service-name]
```

## Health Checks

The scripts automatically wait for all services to become healthy before completing. You can manually check service health:

```bash
# Check individual service health
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Auth Service
curl http://localhost:8083/actuator/health  # Wallet Service
curl http://localhost:8084/actuator/health  # Payment Service
curl http://localhost:8085/actuator/health  # Transaction Service
curl http://localhost:8086/actuator/health  # Notification Service
```

## Troubleshooting

### Port Conflicts
If you encounter port conflicts, make sure the following ports are available:
- 80 (Frontend/Nginx)
- 8080 (API Gateway)
- 8081 (Auth Service)
- 8083 (Wallet Service)
- 8084 (Payment Service)
- 8085 (Transaction Service)
- 8086 (Notification Service)
- 8761 (Eureka Server)
- 9090 (Prometheus)
- 9092 (Kafka)
- 3001 (Grafana)

### Service Not Starting
1. Check Docker is running: `docker info`
2. Check available disk space: `docker system df`
3. View service logs for errors: `docker-compose logs [service-name]`

### Health Check Timeouts
The scripts wait up to 5 minutes for each service to become healthy. If services take longer to start, you can:
1. Check the service logs for startup issues
2. Increase the timeout values in the scripts
3. Run the health checks manually after startup

## Development vs Production

These scripts use the `docker-compose.prod.yml` file which is configured for production-like environments. For development, you might want to use `docker-compose.yml` instead.

## Next Steps

After starting the stack:
1. Access the frontend at http://localhost
2. Register a new user through the auth service
3. Explore the API documentation at http://localhost:8080/swagger-ui.html
4. Monitor services through Grafana at http://localhost:3001
5. View metrics in Prometheus at http://localhost:9090
