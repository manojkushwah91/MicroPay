package com.micropay.transaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MicroPay Transaction Service Application
 * 
 * README:
 * =======
 * This is the transaction recording service for MicroPay platform.
 * 
 * Purpose:
 * - Record transactions after payments are completed
 * - Maintain transaction history with double-entry bookkeeping
 * - Provide transaction query APIs
 * - Publish transaction events to Kafka
 * 
 * Features:
 * - RESTful API for transaction queries
 * - PostgreSQL database for transaction persistence
 * - Kafka integration for event-driven architecture
 * - Automatic transaction recording from payment.completed events
 * - Double-entry bookkeeping (debit/credit entries)
 * - Transaction history tracking
 * 
 * API Endpoints:
 * - GET /transactions/{userId} - Fetch all transactions for a user
 *   Response: [
 *     {
 *       "transactionId": "uuid",
 *       "paymentId": "uuid",
 *       "status": "RECORDED",
 *       "entries": [
 *         { "userId": "uuid", "entryType": "DEBIT", "amount": 100.00, "currency": "USD" },
 *         { "userId": "uuid", "entryType": "CREDIT", "amount": 100.00, "currency": "USD" }
 *       ],
 *       "recordedAt": "2024-01-01T00:00:00"
 *     }
 *   ]
 * 
 * - GET /transaction/{transactionId} - Fetch transaction details
 *   Response: {
 *     "transactionId": "uuid",
 *     "paymentId": "uuid",
 *     "status": "RECORDED",
 *     "entries": [...],
 *     "recordedAt": "2024-01-01T00:00:00"
 *   }
 * 
 * Database:
 * - PostgreSQL database: micropay_transaction_db
 * - Tables: 
 *   - transactions (id, transaction_id, payment_id, status, created_at, updated_at, recorded_at, ...)
 *   - transaction_entries (id, transaction_id, user_id, entry_type, amount, currency)
 * - Flyway migrations for schema management
 * - Double-entry bookkeeping with debit/credit entries
 * - Optimistic locking using @Version annotation
 * 
 * Kafka Integration:
 * - Consumes 'payment.completed' events from payment-service
 *   - Automatically records transactions when payments are completed
 *   - Creates double-entry bookkeeping entries (debit for payer, credit for payee)
 *   - Topic: payment.completed
 *   - Consumer group: transaction-service-consumer-group
 * 
 * - Produces 'transaction.recorded' events after successfully recording
 *   - Published when transaction is successfully recorded
 *   - Event contains: transactionId, paymentId, entries (debit/credit), status
 *   - Topic: transaction.recorded
 * 
 * Transaction Recording Flow:
 * 1. Payment service completes a payment and publishes payment.completed event
 * 2. Transaction service consumes the event
 * 3. Creates transaction with double-entry bookkeeping:
 *    - Debit entry for payer (money going out)
 *    - Credit entry for payee (money coming in)
 * 4. Saves transaction to database with status RECORDED
 * 5. Publishes transaction.recorded event
 * 
 * Double-Entry Bookkeeping:
 * - Each transaction has at least two entries (debit and credit)
 * - Debit entry: Records money going out (payer)
 * - Credit entry: Records money coming in (payee)
 * - Ensures accounting balance: sum of debits = sum of credits
 * 
 * Error Handling:
 * - TransactionNotFoundException (404): Transaction not found
 * - TransactionProcessingException (500): Transaction recording failed
 * - Global exception handler with proper error responses
 * - Failed transactions are logged with failure reason
 * 
 * Configuration:
 * - Port: 8085 (default)
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
 * - Container exposes port 8085
 * - Requires PostgreSQL and Kafka to be accessible
 * - Environment variables for database and Kafka configuration
 * - Health check endpoint configured
 * 
 * Service Discovery:
 * - Registers with Eureka server
 * - Discoverable as 'transaction-service'
 * 
 * Security Considerations:
 * - In production, add authentication/authorization
 * - Validate user ownership before transaction queries
 * - Implement rate limiting for API endpoints
 * - Add audit logging for all transaction operations
 * 
 * Performance:
 * - Optimistic locking for concurrent transaction updates
 * - Connection pooling via HikariCP
 * - Kafka producer with idempotence enabled
 * - Efficient querying with indexed columns
 * - Eager loading of transaction entries for API responses
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class TransactionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionServiceApplication.class, args);
    }
}




