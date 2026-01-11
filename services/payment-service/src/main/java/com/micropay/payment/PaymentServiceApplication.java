package com.micropay.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MicroPay Payment Service Application
 * 
 * README:
 * =======
 * This is the payment processing service for MicroPay platform.
 * 
 * Purpose:
 * - Handle payment initiation and processing
 * - Interact with wallet service for balance verification
 * - Process payments based on wallet balance updates
 * - Publish payment lifecycle events to Kafka
 * 
 * Features:
 * - RESTful API for payment operations
 * - PostgreSQL database for payment persistence
 * - Kafka integration for event-driven architecture
 * - Idempotency support using paymentId and idempotency key
 * - Automatic payment processing on wallet balance updates
 * - Payment lifecycle management (initiated, authorized, completed, failed)
 * 
 * API Endpoints:
 * - POST /payment - Initiate a payment
 *   Request: { 
 *     "payerUserId": "uuid", 
 *     "payeeUserId": "uuid", 
 *     "amount": 100.00, 
 *     "currency": "USD",
 *     "paymentType": "PAYMENT",
 *     "description": "Payment description",
 *     "reference": "REF123",
 *     "idempotencyKey": "unique-key"
 *   }
 *   Response: { "paymentId": "uuid", "status": "INITIATED", ... }
 * 
 * - GET /payment/{paymentId} - Fetch payment status
 *   Response: { "paymentId": "uuid", "status": "COMPLETED", "amount": 100.00, ... }
 * 
 * Database:
 * - PostgreSQL database: micropay_payment_db
 * - Table: payments (id, payment_id, idempotency_key, payer_user_id, payee_user_id, 
 *                     amount, currency, payment_type, status, ...)
 * - Flyway migrations for schema management
 * - Unique constraint on payment_id and idempotency_key for idempotency
 * - Optimistic locking using @Version annotation
 * 
 * Kafka Integration:
 * - Consumes 'wallet.balance.updated' events from wallet-service
 *   - Automatically processes pending payments when wallet balance is updated
 *   - Checks for sufficient balance and triggers payment authorization/completion
 *   - Topic: wallet.balance.updated
 *   - Consumer group: payment-service-consumer-group
 * 
 * - Produces payment lifecycle events:
 *   - 'payment.initiated' - When payment is created
 *   - 'payment.authorized' - When payment is authorized (sufficient balance)
 *   - 'payment.completed' - When payment is successfully completed
 *   - 'payment.failed' - When payment fails (insufficient balance, errors, etc.)
 * 
 * Idempotency:
 * - Implemented using idempotency key in payment request
 * - Duplicate requests with same idempotency key return existing payment
 * - Unique constraint on idempotency_key prevents duplicate payments
 * - Payment ID is also unique for additional idempotency check
 * 
 * Payment Flow:
 * 1. Client initiates payment via POST /payment
 * 2. Payment is created with status INITIATED
 * 3. payment.initiated event is published
 * 4. Payment waits for wallet balance update
 * 5. When wallet.balance.updated event is received:
 *    - Check if balance is sufficient
 *    - If sufficient: authorize payment → complete payment
 *    - If insufficient: fail payment
 * 6. Payment lifecycle events are published accordingly
 * 
 * Error Handling:
 * - PaymentNotFoundException (404): Payment not found
 * - DuplicatePaymentException (409): Duplicate payment detected (idempotency violation)
 * - Validation errors (400): Invalid request parameters
 * - Global exception handler with proper error responses
 * 
 * Configuration:
 * - Port: 8084 (default)
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
 * - Container exposes port 8084
 * - Requires PostgreSQL and Kafka to be accessible
 * - Environment variables for database and Kafka configuration
 * - Health check endpoint configured
 * 
 * Service Discovery:
 * - Registers with Eureka server
 * - Discoverable as 'payment-service'
 * 
 * Security Considerations:
 * - In production, add authentication/authorization
 * - Validate user ownership before payment operations
 * - Implement rate limiting for API endpoints
 * - Add audit logging for all payment operations
 * 
 * Performance:
 * - Optimistic locking for concurrent payment updates
 * - Connection pooling via HikariCP
 * - Kafka producer with idempotence enabled
 * - Efficient querying with indexed columns
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}




