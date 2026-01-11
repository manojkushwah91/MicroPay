# MicroPay API Gateway

Single entry point API Gateway for all MicroPay microservices using Spring Cloud Gateway.

## Overview

The API Gateway provides a unified entry point for all external requests to MicroPay microservices. It handles routing, load balancing, security, and cross-cutting concerns.

## Features

- **Request Routing**: Routes requests to appropriate microservices via Eureka
- **Load Balancing**: Client-side load balancing across service instances
- **Service Discovery**: Integrates with Eureka for service lookup
- **Security**: Spring Security enabled (JWT placeholder for future implementation)
- **CORS Support**: Configured for frontend applications
- **Actuator Endpoints**: Health checks and gateway route information
- **Dynamic Routing**: Route configuration via application.yml

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Cloud Gateway 2023.0.0
- Spring Cloud Netflix Eureka Client
- Spring Security (WebFlux)
- Spring Boot Actuator

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (optional, for containerization)
- Eureka Server (required for service discovery)

## Configuration

### Application Configuration

The gateway runs on port **8080** by default.

### Route Configuration

Routes are configured in `application.yml`:

- **User Service**: `/api/v1/users/**` → `lb://user-service`
- **Account Service**: `/api/v1/accounts/**` → `lb://account-service`
- **Payment Service**: `/api/v1/payments/**` → `lb://payment-service`
- **Transaction Service**: `/api/v1/transactions/**` → `lb://transaction-service`
- **Balance Service**: `/api/v1/balances/**` → `lb://balance-service`
- **Notification Service**: `/api/v1/notifications/**` → `lb://notification-service`
- **Reporting Service**: `/api/v1/reports/**` → `lb://reporting-service`

### Security Configuration

**Public Endpoints** (no authentication):
- `/actuator/**`
- `/api/v1/users/register`
- `/api/v1/users/login`
- `/api/v1/users/reset-password`
- `/api/v1/users/verify-email`

**Protected Endpoints** (authentication required):
- All other `/api/v1/**` routes

**Note**: JWT authentication is a placeholder and will be implemented in future iterations.

### Environment Variables

- `SERVER_PORT`: Gateway port (default: 8080)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL (default: http://localhost:8761/eureka)

## Running the Application

### Local Development

1. Ensure Eureka Server is running on port 8761

2. Run the gateway:
   ```bash
   mvn spring-boot:run
   ```

### Using Maven

```bash
mvn clean package
java -jar target/api-gateway-1.0.0.jar
```

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t micropay/api-gateway:1.0.0 .
   ```

2. Run the container:
   ```bash
   docker run -d \
     -p 8080:8080 \
     -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka \
     --name api-gateway \
     micropay/api-gateway:1.0.0
   ```

## API Endpoints

### Service Routes

All service routes are prefixed with `/api/v1/`:

- **User Service**: `http://localhost:8080/api/v1/users/**`
- **Account Service**: `http://localhost:8080/api/v1/accounts/**`
- **Payment Service**: `http://localhost:8080/api/v1/payments/**`
- **Transaction Service**: `http://localhost:8080/api/v1/transactions/**`
- **Balance Service**: `http://localhost:8080/api/v1/balances/**`
- **Notification Service**: `http://localhost:8080/api/v1/notifications/**`
- **Reporting Service**: `http://localhost:8080/api/v1/reports/**`

### Actuator Endpoints

- **Health Check**: `GET /actuator/health`
- **Service Info**: `GET /actuator/info`
- **Gateway Routes**: `GET /actuator/gateway/routes`
- **Refresh Routes**: `POST /actuator/gateway/refresh`

## Load Balancing

The gateway uses Eureka service discovery for load balancing:

- Automatically discovers service instances via Eureka
- Routes requests to healthy service instances only
- Implements round-robin load balancing (default)
- Supports multiple instances of the same service

## CORS Configuration

CORS is configured for the following origins:
- `http://localhost:3000` (React development)
- `http://localhost:5173` (Vite development)

For production, update the `allowedOrigins` in `application.yml`.

## Security

### Current Implementation

- Spring Security enabled
- Public endpoints configured (registration, login)
- Protected endpoints require authentication (placeholder)
- CSRF disabled for API endpoints

### Future Enhancements

- JWT token validation
- Rate limiting (Redis-based)
- Request/response transformation
- API versioning
- Circuit breakers (Resilience4j)
- Distributed tracing

## Development

### Project Structure

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/micropay/gateway/
│   │   │   ├── ApiGatewayApplication.java
│   │   │   └── config/
│   │   │       └── SecurityConfig.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

### Building

```bash
mvn clean package
```

### Testing

```bash
mvn test
```

## Troubleshooting

### Routes Not Working

- Verify Eureka Server is running and accessible
- Check service registration in Eureka dashboard
- Verify route configuration in `application.yml`
- Check gateway logs for routing errors

### Service Discovery Issues

- Verify Eureka client configuration
- Check `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` environment variable
- Ensure services are registered with Eureka
- Review Eureka dashboard for registered services

### CORS Errors

- Verify CORS configuration in `application.yml`
- Check allowed origins match frontend URL
- Ensure preflight requests (OPTIONS) are handled

### Security Issues

- Check security configuration in `SecurityConfig.java`
- Verify public endpoints are correctly configured
- Review security filter chain order

## Production Considerations

1. **High Availability**: Deploy multiple gateway instances behind load balancer
2. **Rate Limiting**: Implement Redis-based rate limiting
3. **JWT Authentication**: Implement JWT token validation
4. **Monitoring**: Set up alerts for gateway errors and latency
5. **Circuit Breakers**: Add circuit breakers for downstream services
6. **Request/Response Logging**: Implement structured logging
7. **SSL/TLS**: Configure HTTPS for production
8. **API Versioning**: Implement versioning strategy

## License

Copyright © 2024 MicroPay. All rights reserved.

