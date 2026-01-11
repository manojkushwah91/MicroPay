# MicroPay Eureka Discovery Server

Service discovery server for MicroPay microservices using Spring Cloud Netflix Eureka.

## Overview

The Eureka Server provides service registration and discovery for all MicroPay microservices. It enables load balancing, health monitoring, and automatic service instance management.

## Features

- **Service Registration**: Microservices register themselves on startup
- **Service Discovery**: Services can discover other services via Eureka
- **Health Monitoring**: Tracks service health and availability
- **Load Balancing**: Enables client-side load balancing across service instances
- **Self-Preservation**: Prevents registry cleanup in case of network issues
- **Dashboard UI**: Web-based dashboard for viewing registered services

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Cloud Netflix Eureka Server 2023.0.0
- Spring Boot Actuator

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (optional, for containerization)

## Configuration

### Application Configuration

The server runs on port **8761** by default.

### Key Configuration Properties

- **Self-Preservation**: Enabled (prevents registry cleanup)
- **Eviction Interval**: 60 seconds
- **Renewal Threshold**: 85% of services must renew within 15 minutes
- **Response Cache Update**: 30 seconds

### Environment Variables

- `SERVER_PORT`: Server port (default: 8761)
- `EUREKA_INSTANCE_HOSTNAME`: Eureka instance hostname (default: localhost)

## Running the Application

### Local Development

```bash
mvn spring-boot:run
```

### Using Maven

```bash
mvn clean package
java -jar target/eureka-server-1.0.0.jar
```

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t micropay/eureka-server:1.0.0 .
   ```

2. Run the container:
   ```bash
   docker run -d \
     -p 8761:8761 \
     --name eureka-server \
     micropay/eureka-server:1.0.0
   ```

## API Endpoints

### Eureka Dashboard

- **Dashboard UI**: `http://localhost:8761`
- **Service Registry (JSON)**: `http://localhost:8761/eureka/apps`
- **Service Instance Info**: `http://localhost:8761/eureka/apps/{service-name}`

### Actuator Endpoints

- **Health Check**: `GET /actuator/health`
- **Service Info**: `GET /actuator/info`

## Service Registration

Microservices register with Eureka by including the Eureka client dependency and configuration:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka
    register-with-eureka: true
    fetch-registry: true
```

## High Availability

For production deployments, configure multiple Eureka instances with peer-to-peer replication:

```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server-1:8761/eureka,http://eureka-server-2:8761/eureka
```

Each Eureka instance should:
1. Register with other Eureka instances
2. Fetch registry from other instances
3. Replicate service registrations

## Monitoring

### Key Metrics

- Number of registered services
- Number of service instances per service
- Service renewal rate
- Eviction count
- Self-preservation mode status

### Health Checks

- Eureka server health
- Registry health
- Service instance health (via heartbeats)

## Troubleshooting

### Services Not Appearing in Dashboard

- Verify service is running and can reach Eureka server
- Check service configuration (register-with-eureka: true)
- Check network connectivity
- Review service logs for registration errors

### Services Disappearing from Registry

- Check service health (heartbeats)
- Verify lease renewal interval configuration
- Check self-preservation mode status
- Review eviction logs

### High Memory Usage

- Adjust eviction interval
- Review number of registered services
- Consider increasing JVM heap size

## Development

### Project Structure

```
eureka-server/
├── src/
│   ├── main/
│   │   ├── java/com/micropay/eureka/
│   │   │   └── EurekaServerApplication.java
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

## Production Considerations

1. **High Availability**: Deploy multiple Eureka instances
2. **Monitoring**: Set up alerts for service registration failures
3. **Resource Limits**: Configure appropriate memory and CPU limits
4. **Network**: Ensure stable network connectivity between Eureka and services
5. **Security**: Add authentication for production (not included in this version)

## License

Copyright © 2024 MicroPay. All rights reserved.

