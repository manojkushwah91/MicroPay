# MicroPay - Docker Production Setup Guide

## Overview

This guide provides complete instructions for running the MicroPay microservices application in production using Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+
- 8GB+ RAM recommended
- 20GB+ free disk space

## Quick Start

### 1. Create Environment File

```bash
cp .env.example .env
```

Edit `.env` and set your database password and other secrets:

```env
POSTGRES_PASSWORD=your_secure_password_here
JWT_SECRET=your_jwt_secret_key_change_in_production
```

### 2. Build and Start All Services

```bash
cd infrastructure/docker
docker-compose up -d --build
```

### 3. Verify Services

Check service health:
```bash
docker-compose ps
```

View logs:
```bash
docker-compose logs -f
```

## Service Architecture

### Infrastructure Services
- **postgres**: PostgreSQL 15 database
- **zookeeper**: Zookeeper for Kafka
- **kafka**: Apache Kafka message broker
- **kafka-init**: Creates Kafka topics on startup

### Spring Cloud Services
- **config-server**: Centralized configuration (port 8888)
- **eureka-server**: Service discovery (port 8761)

### Microservices
- **api-gateway**: API Gateway (port 8080)
- **auth-service**: Authentication service (port 8081)
- **wallet-service**: Wallet management (port 8083)
- **payment-service**: Payment processing (port 8084)
- **transaction-service**: Transaction recording (port 8085)
- **notification-service**: Notifications (port 8086)

### Frontend
- **frontend**: React frontend with Nginx (port 3000)

## Service Dependencies

```
postgres → (healthy)
zookeeper → (healthy)
kafka → zookeeper (healthy)
kafka-init → kafka (healthy)
config-server → (healthy)
eureka-server → config-server (healthy)
api-gateway → eureka-server, config-server (healthy)
auth-service → postgres, kafka, eureka-server, config-server (healthy)
wallet-service → postgres, kafka, eureka-server, config-server (healthy)
payment-service → postgres, kafka, eureka-server, config-server (healthy)
transaction-service → postgres, kafka, eureka-server, config-server (healthy)
notification-service → postgres, kafka, eureka-server, config-server (healthy)
frontend → api-gateway (healthy)
```

## Access Points

- **Frontend**: http://localhost:3000
- **API Gateway**: http://localhost:8080
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **PostgreSQL**: localhost:5432

## Environment Variables

All services use environment variables from `.env` file. Key variables:

- `POSTGRES_PASSWORD`: Database password (REQUIRED)
- `POSTGRES_USER`: Database user (default: postgres)
- `JWT_SECRET`: JWT signing secret (REQUIRED for production)
- `EUREKA_URL`: Eureka server URL
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka broker address

## Health Checks

All services include health checks:
- Infrastructure: 10-40s start period
- Spring Boot services: 60-120s start period
- Frontend: 10s start period

Check health status:
```bash
docker-compose ps
```

## Logs

View all logs:
```bash
docker-compose logs -f
```

View specific service:
```bash
docker-compose logs -f api-gateway
docker-compose logs -f auth-service
```

## Database

### Initialization

Database is automatically initialized with:
- 5 databases (one per service)
- All required tables via Flyway migrations

### Access Database

```bash
docker exec -it micropay-postgres psql -U postgres
```

### Backup

```bash
docker exec micropay-postgres pg_dumpall -U postgres > backup.sql
```

### Restore

```bash
docker exec -i micropay-postgres psql -U postgres < backup.sql
```

## Kafka Topics

Topics are automatically created by `kafka-init` service:
- `user.created`
- `wallet.balance.updated`
- `payment.initiated`
- `payment.authorized`
- `payment.completed`
- `payment.failed`
- `transaction.recorded`
- `notification.send`

## Troubleshooting

### Services Not Starting

1. Check logs:
   ```bash
   docker-compose logs <service-name>
   ```

2. Verify dependencies are healthy:
   ```bash
   docker-compose ps
   ```

3. Check environment variables:
   ```bash
   docker-compose config
   ```

### Database Connection Issues

1. Verify PostgreSQL is healthy:
   ```bash
   docker-compose ps postgres
   ```

2. Check database URL in service logs
3. Verify `POSTGRES_PASSWORD` is set in `.env`

### Eureka Registration Issues

1. Check Eureka dashboard: http://localhost:8761
2. Verify service hostname matches container name
3. Check `EUREKA_INSTANCE_HOSTNAME` environment variable

### Frontend Can't Connect to API

1. Verify API Gateway is running: http://localhost:8080/actuator/health
2. Check browser console for CORS errors
3. Verify `VITE_API_BASE_URL` in frontend build

### Port Conflicts

If ports are already in use, modify `docker-compose.yml` or stop conflicting services.

## Scaling Services

Scale a service:
```bash
docker-compose up -d --scale wallet-service=3
```

Note: Ensure your load balancer (if any) is configured correctly.

## Production Considerations

### Security

1. **Change Default Passwords**: Update all default passwords in `.env`
2. **JWT Secret**: Use a strong, random JWT secret
3. **Database**: Use strong database passwords
4. **Network**: Consider using Docker networks with restricted access
5. **SSL/TLS**: Add reverse proxy (nginx/traefik) with SSL certificates

### Performance

1. **Resource Limits**: Add resource limits to services in `docker-compose.yml`
2. **Database**: Tune PostgreSQL settings for your workload
3. **Kafka**: Configure Kafka for production (replication, partitions)
4. **Monitoring**: Add Prometheus/Grafana for monitoring

### High Availability

1. **Database**: Use PostgreSQL replication
2. **Kafka**: Use multi-broker Kafka cluster
3. **Services**: Run multiple instances behind load balancer
4. **Eureka**: Run multiple Eureka instances in peer mode

## Maintenance

### Update Services

1. Pull latest code
2. Rebuild images:
   ```bash
   docker-compose build
   ```
3. Restart services:
   ```bash
   docker-compose up -d
   ```

### Clean Up

Remove all containers and volumes:
```bash
docker-compose down -v
```

Remove only containers (keep volumes):
```bash
docker-compose down
```

## Monitoring

### Service Health

```bash
# All services
docker-compose ps

# Specific service health endpoint
curl http://localhost:8080/actuator/health
```

### Resource Usage

```bash
docker stats
```

## Support

For issues or questions:
1. Check service logs
2. Verify environment variables
3. Check Eureka dashboard for service registration
4. Review this documentation

---

**Last Updated**: 2024
**Version**: 1.0.0


