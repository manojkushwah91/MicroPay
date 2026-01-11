# MicroPay Authentication Service

Production-ready authentication and authorization service for MicroPay platform.

## Overview

The Auth Service handles user registration, login, and JWT token management. It integrates with PostgreSQL for user storage and Kafka for event publishing.

## Features

- **User Registration**: Create new user accounts with email and password
- **User Login**: Authenticate users and generate JWT tokens
- **JWT Authentication**: Stateless token-based authentication
- **Password Encryption**: BCrypt password hashing
- **Role-Based Access Control**: RBAC support (OAuth2 ready)
- **Kafka Integration**: Publishes `user.created` events on registration
- **PostgreSQL**: User and role storage
- **Service Discovery**: Eureka client integration

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Spring Security
- JWT (JJWT 0.12.3)
- PostgreSQL
- Spring Kafka
- Spring Cloud (Config, Eureka)

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12+
- Kafka (for event publishing)
- Eureka Server (for service discovery)
- Docker (optional, for containerization)

## API Endpoints

### Register User

**POST** `/auth/register`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

### Login

**POST** `/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe"
}
```

## Configuration

### Application Configuration

The service runs on port **8081** by default.

### Environment Variables

- `DB_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/micropay_auth_db`)
- `DB_USERNAME`: Database username (default: `auth_user`)
- `DB_PASSWORD`: Database password (default: `password`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `JWT_SECRET`: JWT signing secret (default: provided, but should be changed in production)
- `JWT_EXPIRATION`: JWT expiration in milliseconds (default: `86400000` = 24 hours)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL (default: `http://localhost:8761/eureka`)

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE micropay_auth_db;
CREATE USER auth_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE micropay_auth_db TO auth_user;
```

2. The service will create tables automatically on first run (using Flyway migrations).

## Running the Application

### Local Development

1. Ensure PostgreSQL, Kafka, and Eureka are running

2. Run the application:
```bash
mvn spring-boot:run
```

### Using Maven

```bash
mvn clean package
java -jar target/auth-service-1.0.0.jar
```

### Using Docker

1. Build the Docker image:
```bash
docker build -t micropay/auth-service:1.0.0 .
```

2. Run the container:
```bash
docker run -d \
  -p 8081:8081 \
  -e DB_URL=jdbc:postgresql://postgres:5432/micropay_auth_db \
  -e DB_USERNAME=auth_user \
  -e DB_PASSWORD=password \
  -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
  -e JWT_SECRET=your-secret-key-here \
  -e EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka \
  --name auth-service \
  micropay/auth-service:1.0.0
```

## Kafka Integration

### Events Published

**Topic**: `user.created`

**Event Structure:**
```json
{
  "eventId": "uuid",
  "eventType": "user.created",
  "timestamp": "2024-01-15T10:00:00",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2024-01-15T10:00:00"
}
```

## Security

### JWT Token

- **Algorithm**: HS256
- **Expiration**: 24 hours (configurable)
- **Claims**: username (email), roles, issued at, expiration

### Password Security

- **Hashing**: BCrypt with strength 10
- **Minimum Length**: 8 characters
- **Validation**: Email format, required fields

## Database Schema

### Tables

- **users**: User accounts
  - id (UUID)
  - email (unique)
  - password (BCrypt hash)
  - first_name, last_name
  - status (ACTIVE, INACTIVE, SUSPENDED, DELETED)
  - created_at, updated_at, last_login_at

- **roles**: User roles
  - id (UUID)
  - name (unique)
  - description

- **user_roles**: User-role mapping
  - user_id, role_id

## Development

### Project Structure

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/com/micropay/auth/
│   │   │   ├── AuthServiceApplication.java
│   │   │   ├── controller/
│   │   │   │   └── AuthController.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   └── JwtService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── RoleRepository.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Role.java
│   │   │   │   └── UserStatus.java
│   │   │   ├── dto/
│   │   │   │   ├── RegisterRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   └── UserCreatedEvent.java
│   │   │   └── config/
│   │   │       ├── SecurityConfig.java
│   │   │       └── KafkaConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
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

### Database Connection Issues

1. Verify PostgreSQL is running:
```bash
psql -U auth_user -d micropay_auth_db
```

2. Check connection URL in `application.yml`

3. Verify database credentials

### Kafka Connection Issues

1. Verify Kafka is running:
```bash
docker exec -it kafka kafka-topics --list --bootstrap-server localhost:9092
```

2. Check Kafka bootstrap servers configuration

3. Verify network connectivity

### JWT Token Issues

1. Verify JWT secret is configured
2. Check token expiration settings
3. Ensure token is not expired

## Production Considerations

1. **JWT Secret**: Use a strong, randomly generated secret (at least 256 bits)
2. **Database**: Use connection pooling and read replicas
3. **Kafka**: Enable idempotent producers and proper error handling
4. **Security**: Enable HTTPS, add rate limiting, implement token refresh
5. **Monitoring**: Set up health checks, metrics, and alerts
6. **High Availability**: Deploy multiple instances behind load balancer
7. **Password Policy**: Implement stronger password requirements
8. **Account Lockout**: Implement account lockout after failed login attempts

## License

Copyright © 2024 MicroPay. All rights reserved.

