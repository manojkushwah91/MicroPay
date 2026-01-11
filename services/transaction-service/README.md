# Transaction Service

MicroPay Transaction Recording Service - Records transactions after payments and produces transaction events.

## Overview

The transaction service is responsible for:
- Recording transactions when payments are completed
- Maintaining transaction history with double-entry bookkeeping
- Providing transaction query APIs
- Publishing transaction events to Kafka

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka
- **Build Tool**: Maven
- **Service Discovery**: Eureka

## API Endpoints

### GET /transactions/{userId}
Fetch all transactions for a user.

**Response:**
```json
[
  {
    "transactionId": "uuid",
    "paymentId": "uuid",
    "status": "RECORDED",
    "entries": [
      {
        "userId": "uuid",
        "entryType": "DEBIT",
        "amount": 100.00,
        "currency": "USD"
      },
      {
        "userId": "uuid",
        "entryType": "CREDIT",
        "amount": 100.00,
        "currency": "USD"
      }
    ],
    "recordedAt": "2024-01-01T00:00:00"
  }
]
```

### GET /transaction/{transactionId}
Fetch transaction details.

**Response:**
```json
{
  "transactionId": "uuid",
  "paymentId": "uuid",
  "status": "RECORDED",
  "entries": [
    {
      "userId": "uuid",
      "entryType": "DEBIT",
      "amount": 100.00,
      "currency": "USD"
    },
    {
      "userId": "uuid",
      "entryType": "CREDIT",
      "amount": 100.00,
      "currency": "USD"
    }
  ],
  "recordedAt": "2024-01-01T00:00:00"
}
```

## Kafka Integration

### Consumes
- **Topic**: `payment.completed`
- **Purpose**: Record transactions when payments are completed
- **Consumer Group**: `transaction-service-consumer-group`
- **Behavior**: 
  - Creates transaction with double-entry bookkeeping
  - Debit entry for payer (money going out)
  - Credit entry for payee (money coming in)

### Produces
- **Topic**: `transaction.recorded`
  - Published when transaction is successfully recorded
  - Event contains: transactionId, paymentId, entries, status

## Database Schema

The service uses PostgreSQL with the following tables:

**transactions**
- `id` (UUID, Primary Key)
- `transaction_id` (UUID, Unique, Not Null)
- `payment_id` (UUID, Not Null)
- `status` (VARCHAR(20), Not Null, Default: 'PENDING')
- `created_at` (TIMESTAMP, Not Null)
- `updated_at` (TIMESTAMP, Not Null)
- `recorded_at` (TIMESTAMP)
- `failed_at` (TIMESTAMP)
- `failure_reason` (VARCHAR(500))
- `version` (BIGINT, Not Null, Default: 0) - Optimistic locking

**transaction_entries**
- `id` (UUID, Primary Key)
- `transaction_id` (UUID, Foreign Key, Not Null)
- `user_id` (UUID, Not Null)
- `entry_type` (VARCHAR(10), Not Null) - DEBIT or CREDIT
- `amount` (DECIMAL(19,2), Not Null)
- `currency` (VARCHAR(3), Not Null, Default: 'USD')

## Double-Entry Bookkeeping

The service implements double-entry bookkeeping principles:
- Each transaction has at least two entries (debit and credit)
- Debit entry: Records money going out (payer)
- Credit entry: Records money coming in (payee)
- Ensures accounting balance: sum of debits = sum of credits

## Transaction Recording Flow

1. Payment service completes a payment and publishes `payment.completed` event
2. Transaction service consumes the event
3. Creates transaction with double-entry bookkeeping entries
4. Saves transaction to database with status `RECORDED`
5. Publishes `transaction.recorded` event

## Configuration

### Environment Variables

- `DB_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/micropay_transaction_db`)
- `DB_USERNAME`: Database username (default: `transaction_user`)
- `DB_PASSWORD`: Database password (default: `password`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL (default: `http://localhost:8761/eureka`)

### Port
Default port: **8085**

## Building and Running

### Local Development

1. Ensure PostgreSQL and Kafka are running
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   java -jar target/transaction-service-1.0.0.jar
   ```

### Docker

1. Build the Docker image:
   ```bash
   docker build -t transaction-service:1.0.0 .
   ```
2. Run the container:
   ```bash
   docker run -p 8085:8085 \
     -e DB_URL=jdbc:postgresql://postgres:5432/micropay_transaction_db \
     -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
     transaction-service:1.0.0
   ```

## Health Checks

- Health endpoint: `http://localhost:8085/actuator/health`
- Info endpoint: `http://localhost:8085/actuator/info`

## Error Handling

The service handles the following errors:

- **404 Not Found**: Transaction not found
- **500 Internal Server Error**: Transaction processing failed
- **500 Internal Server Error**: Unexpected errors

## Notes

- The service uses optimistic locking for concurrent transaction updates
- Transaction recording is event-driven based on payment completion
- All transactions use double-entry bookkeeping
- Transaction entries are eagerly loaded for API responses
- Failed transactions are logged with failure reason




