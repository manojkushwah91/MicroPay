# Wallet Service

MicroPay Wallet Management Service - Manages user wallets, balances, and Kafka events.

## Overview

The wallet service is responsible for:
- Managing user wallets and balances
- Processing credit and debit operations
- Automatically creating wallets for new users
- Publishing wallet balance update events

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka
- **Build Tool**: Maven
- **Service Discovery**: Eureka

## API Endpoints

### GET /wallet/{userId}
Fetch wallet information for a user.

**Response:**
```json
{
  "id": "uuid",
  "userId": "uuid",
  "balance": 100.00,
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

### POST /wallet/{userId}/credit
Credit amount to user's wallet.

**Request:**
```json
{
  "amount": 50.00,
  "transactionId": "uuid",
  "description": "Payment received"
}
```

**Response:** Updated wallet information

### POST /wallet/{userId}/debit
Debit amount from user's wallet.

**Request:**
```json
{
  "amount": 25.00,
  "transactionId": "uuid",
  "description": "Payment made"
}
```

**Response:** Updated wallet information

**Error:** Returns 400 Bad Request if insufficient balance

## Kafka Integration

### Consumes
- **Topic**: `user.created`
- **Purpose**: Automatically create wallet when new user registers
- **Consumer Group**: `wallet-service-consumer-group`

### Produces
- **Topic**: `wallet.balance.updated`
- **Purpose**: Notify other services when wallet balance changes
- **Event Schema**: See `WalletBalanceUpdatedEvent` class

## Database Schema

The service uses PostgreSQL with the following table:

**wallets**
- `id` (UUID, Primary Key)
- `user_id` (UUID, Unique, Not Null)
- `balance` (DECIMAL(19,2), Not Null, Default: 0.00)
- `currency` (VARCHAR(3), Not Null, Default: 'USD')
- `status` (VARCHAR(20), Not Null, Default: 'ACTIVE')
- `created_at` (TIMESTAMP, Not Null)
- `updated_at` (TIMESTAMP, Not Null)
- `version` (BIGINT, Not Null, Default: 0) - Optimistic locking

## Configuration

### Environment Variables

- `DB_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/micropay_wallet_db`)
- `DB_USERNAME`: Database username (default: `wallet_user`)
- `DB_PASSWORD`: Database password (default: `password`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL (default: `http://localhost:8761/eureka`)

### Port
Default port: **8083**

## Building and Running

### Local Development

1. Ensure PostgreSQL and Kafka are running
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   java -jar target/wallet-service-1.0.0.jar
   ```

### Docker

1. Build the Docker image:
   ```bash
   docker build -t wallet-service:1.0.0 .
   ```
2. Run the container:
   ```bash
   docker run -p 8083:8083 \
     -e DB_URL=jdbc:postgresql://postgres:5432/micropay_wallet_db \
     -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
     wallet-service:1.0.0
   ```

## Health Checks

- Health endpoint: `http://localhost:8083/actuator/health`
- Info endpoint: `http://localhost:8083/actuator/info`

## Error Handling

The service handles the following errors:

- **404 Not Found**: Wallet not found for user
- **400 Bad Request**: Insufficient balance or validation errors
- **500 Internal Server Error**: Unexpected errors

## Notes

- The service uses pessimistic locking for balance updates to prevent race conditions
- Optimistic locking is used for read operations
- Wallet creation is idempotent (won't create duplicate wallets)
- All balance changes are published to Kafka




