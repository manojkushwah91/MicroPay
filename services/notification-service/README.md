# Notification Service

MicroPay Notification Service - Send notifications after payments and transactions.

## Overview

The notification service is responsible for:
- Sending notifications when payments are completed
- Sending notifications when transactions are recorded
- Maintaining notification history
- Providing notification query APIs
- Publishing notification events to Kafka

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Database**: PostgreSQL
- **Messaging**: Apache Kafka
- **Build Tool**: Maven
- **Service Discovery**: Eureka

## API Endpoints

### GET /notifications/{userId}
Fetch user notifications.

**Query Parameters:**
- `page` (default: 0) - Page number
- `size` (default: 20) - Page size

**Response:**
```json
[
  {
    "id": "uuid",
    "userId": "uuid",
    "notificationType": "PAYMENT_COMPLETED",
    "channel": "IN_APP",
    "status": "SENT",
    "title": "Payment Completed",
    "message": "Your payment of 100.00 USD has been completed successfully...",
    "referenceId": "uuid",
    "referenceType": "PAYMENT",
    "createdAt": "2024-01-01T00:00:00",
    "sentAt": "2024-01-01T00:00:00"
  }
]
```

## Kafka Integration

### Consumes
- **Topic**: `payment.completed`
  - Sends notifications to payer and payee when payment is completed
  - Consumer Group: `notification-service-consumer-group`

- **Topic**: `transaction.recorded`
  - Sends notifications to all users involved in the transaction
  - Consumer Group: `notification-service-consumer-group`

### Produces
- **Topic**: `notification.send`
  - Published when notification is successfully sent
  - Event contains: notificationId, userId, notificationType, channel, status

## Database Schema

The service uses PostgreSQL with the following table:

**notifications**
- `id` (UUID, Primary Key)
- `user_id` (UUID, Not Null)
- `notification_type` (VARCHAR(50), Not Null)
- `channel` (VARCHAR(20), Not Null) - EMAIL, SMS, PUSH, IN_APP
- `status` (VARCHAR(20), Not Null, Default: 'PENDING')
- `title` (VARCHAR(200), Not Null)
- `message` (VARCHAR(1000), Not Null)
- `reference_id` (UUID) - paymentId or transactionId
- `reference_type` (VARCHAR(50)) - PAYMENT, TRANSACTION
- `created_at` (TIMESTAMP, Not Null)
- `sent_at` (TIMESTAMP)
- `failed_at` (TIMESTAMP)
- `failure_reason` (VARCHAR(500))
- `version` (BIGINT, Not Null, Default: 0) - Optimistic locking

## Notification Channels

- **EMAIL**: Email notifications (placeholder - not implemented)
- **SMS**: SMS notifications (placeholder - not implemented)
- **PUSH**: Push notifications (placeholder - not implemented)
- **IN_APP**: In-app notifications (stored in database)

## Notification Types

- **PAYMENT_COMPLETED**: Sent when payment is completed
- **TRANSACTION_RECORDED**: Sent when transaction is recorded
- **PAYMENT_FAILED**: Sent when payment fails
- **WALLET_BALANCE_UPDATED**: Sent when wallet balance is updated
- **ACCOUNT_VERIFIED**: Sent when account is verified
- **SECURITY_ALERT**: Sent for security-related events

## Placeholder Implementation

The service uses placeholder logic for notification sending:
- Notification sending is simulated with a small delay
- No actual email/SMS/push services are integrated
- All notifications are stored in database as IN_APP notifications
- In production, integrate with:
  - Email: SendGrid, AWS SES, Mailgun, etc.
  - SMS: Twilio, AWS SNS, etc.
  - Push: FCM, APNS, etc.

## Configuration

### Environment Variables

- `DB_URL`: PostgreSQL connection URL (default: `jdbc:postgresql://localhost:5432/micropay_notification_db`)
- `DB_USERNAME`: Database username (default: `notification_user`)
- `DB_PASSWORD`: Database password (default: `password`)
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka server URL (default: `http://localhost:8761/eureka`)

### Port
Default port: **8086**

## Building and Running

### Local Development

1. Ensure PostgreSQL and Kafka are running
2. Build the project:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   java -jar target/notification-service-1.0.0.jar
   ```

### Docker

1. Build the Docker image:
   ```bash
   docker build -t notification-service:1.0.0 .
   ```
2. Run the container:
   ```bash
   docker run -p 8086:8086 \
     -e DB_URL=jdbc:postgresql://postgres:5432/micropay_notification_db \
     -e KAFKA_BOOTSTRAP_SERVERS=kafka:9092 \
     notification-service:1.0.0
   ```

## Health Checks

- Health endpoint: `http://localhost:8086/actuator/health`
- Info endpoint: `http://localhost:8086/actuator/info`

## Error Handling

The service handles errors gracefully:
- Failed notifications are logged with failure reason
- Notification status is updated to FAILED on errors
- Global exception handler with proper error responses

## Notes

- The service uses placeholder logic for notification sending
- All notifications are stored in database for history tracking
- Notification sending is event-driven based on payment and transaction events
- Pagination is supported for notification queries
- Failed notifications are tracked with failure reason







