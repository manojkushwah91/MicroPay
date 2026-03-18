# Configuration Repository

This directory contains configuration files for all MicroPay microservices.

## Structure

Each service has its own configuration file:
- `{service-name}.yml` - Base configuration
- `{service-name}-{profile}.yml` - Environment-specific configuration

## Profiles

- `dev` - Development environment
- `staging` - Staging environment
- `prod` - Production environment

## Example

- `user-service.yml` - Base configuration for user-service
- `user-service-dev.yml` - Development overrides
- `user-service-prod.yml` - Production overrides

## Accessing Configuration

Services access their configuration via:
```
http://config-server:8888/{service-name}/{profile}
```

Example:
```
http://config-server:8888/user-service/dev
http://config-server:8888/payment-service/prod
```

## Security Note

⚠️ **Important**: This directory may contain sensitive information (passwords, API keys, etc.).

- Do NOT commit sensitive data to version control
- Use environment variables or encrypted values for production
- Consider using Spring Cloud Config Server encryption for sensitive values

