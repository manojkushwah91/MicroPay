# Payment Service

MicroPay Payment Processing Service - Handles payments, interacts with wallets, and produces payment events.

## Overview

The payment service is responsible for:
- Initiating and processing payments
- Checking wallet balances via Kafka events
- Managing payment lifecycle (initiated, authorized, completed, failed)
- Publishing payment events to Kafka
- Implementing idempotency for payment operations

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka
- **Build Tool**: Maven
- **Service Discovery**: Eureka

## API Endpoints

### POST /payment
Initiate a payment.

**Request:**
```json
{
  "payerUserId": "uuid",
  "payeeUserId": "uuid",
  "amount": 100.00,
  "currency": "USD",
  "paymentType": "PAYMENT",
  "description": "Payment description",
  "reference": "REF123",
  "idempotencyKey": "unique-key-123"
}
```

**Response:**
```json
{
  "id": "uuid",
  "paymentId": "uuid",
  "payerUserId": "uuid",
  "payeeUserId": "uuid",
  "amount": 100.00,
  "currency": "USD",
  "paymentType": "PAYMENT",
  "status": "INITIATED",
  "createdAt": "2024-01-01T00:00:00"
}
```

### GET /payment/{paymentId}
Fetch payment status.

**Response:**
```json
{
  "paymentId": "uuid",
  "status": "COMPLETED",
  "amount": 100.00,
  "currency": "USD",
  "completedAt": "2024-01-01T00:00:00",
  "transactionId": "uuid"
}
```

## Kafka Integration

### Consumes
- **Topic**: `wallet.balance.updated`
- **Purpose**: Process pending payments when wallet balance is updated
- **Consumer Group**: `payment-service-consumer-group`
- **Behavior**: 
  - Checks for sufficient balance
  - Authorizes and completes payment if balance is sufficient
  - Fails payment if balance is insufficient

### Produces
- **Topic**: `payment.initiated`
  - Published when payment is created
- **Topic**: `payment.authorized`
  - Published when payment is authorized (sufficient balance)
- **Topic**: `payment.completed`
  - Published when payment is successfully completed
- **Topic**: `payment.failed`
  - Published when payment fails

## Database Schema

The service uses PostgreSQL with the following table:

**payments**
- `id` (UUID, Primary Key)
- `payment_id` (UUID, Unique, Not Null) - For idempotency
- `idempotency_key` (VARCHAR(255), Unique, Not Null) - For idempotency
- `payer_user_id` (UUID, Not Null)
- `payee_user_id` (UUID)
- `amount` (DECIMAL(19,2), Not Null)
- `currency` (VARCHAR(3), Not Null, Default: 'USD')
- `payment_type` (VARCHAR(20), Not Null)
- `status` (VARCHAR(20), Not Null, Default: 'INITIATED')
- `failure_reason` (VARCHAR(50))
- `error_code` (VARCHAR(50))
- `error_message` (VARCHAR(500))
- `description` (VARCHAR(500))
- `reference` (VARCHAR(100))
- `transaction_id` (UUID)
- `created_at` (TIMESTAMP, Not Null)
- `updated_at` (TIMESTAMP, Not Null)
- `authorized_at` (TIMESTAMP)
- `completed_at` (TIMESTAMP)
- `failed_at` (TIMESTAMP)
- `version` (BIGINT, Not Null, Default: 0) - Optimistic locking

## Idempotency

The service implements idempotency using:
- **Idempotency Key**: Unique key provided in payment request
- **Payment ID**: Unique identifier for each payment
- **Database Constraints**: Unique constraints on `idempotency_key` and `payment_id`
- **Duplicate Detection**: Returns existing payment if duplicate idempotency key is detected

## Payment Flow

1. Client initiates payment via `POST /payment`
2. Payment is created with status `INITIATED`
3. `payment.initiated` event is published to Kafka
4. Payment waits for wallet balance update
5. When `wallet.balance.updated` event is received:
   - Check if balance is sufficient
   - If sufficient: authorize payment → complete payment
   - If insufficient: fail payment
6. Payment lifecycle events are published accordingly

## Configuration

### Environment Variables

- `DB_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/micropay_payment_db`)
- `DB_USERNAME`: Database username (default: `payment_user`)
- `DB_PASSWORD`: Database password (default: `password`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL (default: `http://localhost:8761/eureka`)

### Port
Default port: **8084**

## Building and Running

### Local Development

1. Ensure PostgreSQL and Kafka are running
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   java -jar target/payment-service-1.0.0.jar
   ```

### Docker

1. Build the Docker image:
   ```bash
   docker build -t payment-service:1.0.0 .
   ```
2. Run the container:
   ```bash
   docker run -p 8084:8084 \
     -e DB_URL=jdbc:postgresql://postgres:5432/micropay_payment_db \
     -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
     payment-service:1.0.0
   ```

## Health Checks

- Health endpoint: `http://localhost:8084/actuator/health`
- Info endpoint: `http://localhost:8084/actuator/info`

## Error Handling

The service handles the following errors:

- **404 Not Found**: Payment not found
- **409 Conflict**: Duplicate payment (idempotency violation)
- **400 Bad Request**: Validation errors
- **500 Internal Server Error**: Unexpected errors

## Notes

- The service uses optimistic locking for concurrent payment updates
- Payment processing is event-driven based on wallet balance updates
- All payment lifecycle events are published to Kafka
- Idempotency is enforced at the database level with unique constraints







