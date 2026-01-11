package com.micropay.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MicroPay Notification Service Application
 * 
 * README:
 * =======
 * This is the notification service for MicroPay platform.
 * 
 * Purpose:
 * - Send notifications after payments and transactions
 * - Maintain notification history
 * - Provide notification query APIs
 * - Publish notification events to Kafka
 * 
 * Features:
 * - RESTful API for notification queries
 * - PostgreSQL database for notification persistence
 * - Kafka integration for event-driven architecture
 * - Automatic notification sending from payment and transaction events
 * - Support for multiple notification channels (EMAIL, SMS, PUSH, IN_APP)
 * - Placeholder notification logic (no real email/SMS integration)
 * 
 * API Endpoints:
 * - GET /notifications/{userId} - Fetch user notifications
 *   Query Parameters:
 *     - page (default: 0) - Page number
 *     - size (default: 20) - Page size
 *   Response: [
 *     {
 *       "id": "uuid",
 *       "userId": "uuid",
 *       "notificationType": "PAYMENT_COMPLETED",
 *       "channel": "IN_APP",
 *       "status": "SENT",
 *       "title": "Payment Completed",
 *       "message": "Your payment of 100.00 USD has been completed...",
 *       "referenceId": "uuid",
 *       "referenceType": "PAYMENT",
 *       "createdAt": "2024-01-01T00:00:00",
 *       "sentAt": "2024-01-01T00:00:00"
 *     }
 *   ]
 * 
 * Database:
 * - PostgreSQL database: micropay_notification_db
 * - Table: notifications (id, user_id, notification_type, channel, status, 
 *                         title, message, reference_id, reference_type, ...)
 * - Flyway migrations for schema management
 * - Optimistic locking using @Version annotation
 * 
 * Kafka Integration:
 * - Consumes 'payment.completed' events from payment-service
 *   - Sends notifications to payer and payee when payment is completed
 *   - Topic: payment.completed
 *   - Consumer group: notification-service-consumer-group
 * 
 * - Consumes 'transaction.recorded' events from transaction-service
 *   - Sends notifications to all users involved in the transaction
 *   - Topic: transaction.recorded
 *   - Consumer group: notification-service-consumer-group
 * 
 * - Produces 'notification.send' events after sending notifications
 *   - Published when notification is successfully sent
 *   - Event contains: notificationId, userId, notificationType, channel, status
 *   - Topic: notification.send
 * 
 * Notification Flow:
 * 1. Payment or transaction event is received from Kafka
 * 2. Service creates notification records for affected users
 * 3. Placeholder logic simulates sending notification (no real email/SMS)
 * 4. Notification status is updated to SENT
 * 5. notification.send event is published to Kafka
 * 
 * Notification Channels:
 * - EMAIL: Email notifications (placeholder - not implemented)
 * - SMS: SMS notifications (placeholder - not implemented)
 * - PUSH: Push notifications (placeholder - not implemented)
 * - IN_APP: In-app notifications (stored in database)
 * 
 * Notification Types:
 * - PAYMENT_COMPLETED: Sent when payment is completed
 * - TRANSACTION_RECORDED: Sent when transaction is recorded
 * - PAYMENT_FAILED: Sent when payment fails
 * - WALLET_BALANCE_UPDATED: Sent when wallet balance is updated
 * - ACCOUNT_VERIFIED: Sent when account is verified
 * - SECURITY_ALERT: Sent for security-related events
 * 
 * Placeholder Implementation:
 * - Notification sending is simulated with a small delay
 * - No actual email/SMS/push services are integrated
 * - All notifications are stored in database as IN_APP notifications
 * - In production, integrate with:
 *   - Email: SendGrid, AWS SES, Mailgun, etc.
 *   - SMS: Twilio, AWS SNS, etc.
 *   - Push: FCM, APNS, etc.
 * 
 * Error Handling:
 * - Failed notifications are logged with failure reason
 * - Notification status is updated to FAILED on errors
 * - Global exception handler with proper error responses
 * 
 * Configuration:
 * - Port: 8086 (default)
 * - Database: PostgreSQL connection via application.yml or environment variables
 * - Kafka: Bootstrap servers via application.yml or environment variables
 * - Eureka: Service discovery integration
 * 
 * Actuator Endpoints:
 * - /actuator/health - Service health status
 * - /actuator/info - Service information
 * - /actuator/metrics - Application metrics
 * - /actuator/prometheus - Prometheus metrics endpoint
 * 
 * Docker:
 * - Container exposes port 8086
 * - Requires PostgreSQL and Kafka to be accessible
 * - Environment variables for database and Kafka configuration
 * - Health check endpoint configured
 * 
 * Service Discovery:
 * - Registers with Eureka server
 * - Discoverable as 'notification-service'
 * 
 * Security Considerations:
 * - In production, add authentication/authorization
 * - Validate user ownership before notification queries
 * - Implement rate limiting for API endpoints
 * - Add audit logging for all notification operations
 * - Secure notification content (PII, sensitive data)
 * 
 * Performance:
 * - Optimistic locking for concurrent notification updates
 * - Connection pooling via HikariCP
 * - Kafka producer with idempotence enabled
 * - Efficient querying with indexed columns
 * - Pagination support for notification queries
 * 
 * Future Enhancements:
 * - Real email/SMS/push notification integration
 * - Notification preferences per user
 * - Notification templates
 * - Retry mechanism for failed notifications
 * - Notification batching
 * - Delivery status tracking
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}




