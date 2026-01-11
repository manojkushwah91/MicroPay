# MicroPay Config Server

Centralized configuration server for MicroPay microservices using Spring Cloud Config Server.

## Overview

The Config Server provides centralized configuration management for all MicroPay microservices. It supports environment-specific configurations and enables dynamic configuration refresh.

## Features

- **Centralized Configuration**: Single source of truth for all service configurations
- **Environment-Specific**: Supports dev, staging, and production profiles
- **Native Profile**: Uses local filesystem for configuration storage
- **Health Monitoring**: Actuator endpoints for health and info
- **Dynamic Refresh**: Configuration changes can be applied without service restarts

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Cloud Config Server 2023.0.0
- Spring Boot Actuator

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker (optional, for containerization)

## Configuration

### Application Configuration

The server runs on port **8888** by default.

Configuration files are stored in the `config-repo` directory (local filesystem).

### Configuration Structure

```
config-repo/
├── user-service.yml
├── user-service-dev.yml
├── user-service-staging.yml
├── user-service-prod.yml
├── payment-service.yml
├── payment-service-dev.yml
└── ...
```

### Environment Variables

- `SPRING_PROFILES_ACTIVE`: Active Spring profile (default: `native`)
- `CONFIG_REPO_PATH`: Path to configuration repository (default: `./config-repo`)

## Running the Application

### Local Development

1. Create configuration repository:
   ```bash
   mkdir -p config-repo
   ```

2. Add configuration files to `config-repo/` directory

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

### Using Maven

```bash
mvn clean package
java -jar target/config-server-1.0.0.jar
```

### Using Docker

1. Build the Docker image:
   ```bash
   docker build -t micropay/config-server:1.0.0 .
   ```

2. Run the container:
   ```bash
   docker run -d \
     -p 8888:8888 \
     -v $(pwd)/config-repo:/app/config-repo \
     -e SPRING_PROFILES_ACTIVE=native \
     --name config-server \
     micropay/config-server:1.0.0
   ```

## API Endpoints

### Configuration Endpoints

- **Get Configuration**: `GET /{application}/{profile}`
  - Example: `GET /user-service/dev`
  - Returns: Configuration for user-service in dev profile

- **Get Configuration with Label**: `GET /{application}/{profile}/{label}`
  - Example: `GET /user-service/dev/master`

### Actuator Endpoints

- **Health Check**: `GET /actuator/health`
- **Service Info**: `GET /actuator/info`

## Accessing Configuration from Services

Services can access configuration using:

```yaml
spring:
  cloud:
    config:
      uri: http://config-server:8888
      name: {service-name}
      profile: {profile}
      label: master
```

## Configuration Refresh

To refresh configuration without restarting services:

1. Update configuration files in `config-repo/`
2. Services with `@RefreshScope` will pick up changes
3. Or call `/actuator/refresh` endpoint on the service

## Health Checks

The Config Server includes health checks for configured repositories:

- Checks if configuration files are accessible
- Validates repository connectivity
- Reports health status via `/actuator/health`

## Logging

Logging is configured in `application.yml`:
- Console logging: INFO level
- Config Server: DEBUG level (for troubleshooting)
- Application: DEBUG level

## Development

### Project Structure

```
config-server/
├── src/
│   ├── main/
│   │   ├── java/com/micropay/config/
│   │   │   └── ConfigServerApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── config-repo/          # Configuration repository
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

### Configuration Not Found

- Verify configuration files exist in `config-repo/`
- Check file naming: `{service-name}-{profile}.yml`
- Ensure `CONFIG_REPO_PATH` environment variable is set correctly

### Port Already in Use

- Change port in `application.yml`: `server.port: 8889`
- Or stop the process using port 8888

### Health Check Fails

- Verify configuration repository is accessible
- Check file permissions on `config-repo/` directory
- Review logs for specific error messages

## Production Considerations

1. **Security**: Add authentication/authorization for production
2. **Encryption**: Encrypt sensitive configuration values
3. **High Availability**: Deploy multiple instances with load balancing
4. **Backup**: Regularly backup configuration repository
5. **Monitoring**: Set up alerts for health check failures
6. **Version Control**: Keep configuration in version control (Git)

## License

Copyright © 2024 MicroPay. All rights reserved.

