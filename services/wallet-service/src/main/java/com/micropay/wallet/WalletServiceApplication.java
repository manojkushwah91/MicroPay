package com.micropay.wallet;

import com.micropay.wallet.config.DotEnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MicroPay Wallet Service Application
 * 
 * README:
 * =======
 * This is the wallet management service for MicroPay platform.
 * 
 * Purpose:
 * - Manage user wallets and balances
 * - Handle wallet credit and debit operations
 * - Automatically create wallets for new users via Kafka events
 * - Publish wallet balance update events to Kafka
 * 
 * Features:
 * - RESTful API for wallet operations
 * - PostgreSQL database for wallet persistence
 * - Kafka integration for event-driven architecture
 * - Automatic wallet creation on user registration
 * - Balance update event publishing
 * - Optimistic locking for concurrent balance updates
 * - Proper error handling for insufficient balance
 * 
 * API Endpoints:
 * - GET /wallet/{userId} - Fetch wallet information
 *   Response: { "id": "uuid", "userId": "uuid", "balance": 100.00, "currency": "USD", "status": "ACTIVE", ... }
 * 
 * - POST /wallet/{userId}/credit - Credit amount to wallet
 *   Request: { "amount": 50.00, "transactionId": "uuid", "description": "Payment received" }
 *   Response: { "id": "uuid", "userId": "uuid", "balance": 150.00, ... }
 * 
 * - POST /wallet/{userId}/debit - Debit amount from wallet
 *   Request: { "amount": 25.00, "transactionId": "uuid", "description": "Payment made" }
 *   Response: { "id": "uuid", "userId": "uuid", "balance": 125.00, ... }
 *   Error: 400 Bad Request if insufficient balance
 * 
 * Database:
 * - PostgreSQL database: micropay_wallet_db
 * - Table: wallets (id, user_id, balance, currency, status, created_at, updated_at, version)
 * - Flyway migrations for schema management
 * - Optimistic locking using @Version annotation
 * - Pessimistic locking for balance updates to prevent race conditions
 * 
 * Kafka Integration:
 * - Consumes 'user.created' events from auth-service
 *   - Automatically creates a wallet when a new user is registered
 *   - Topic: user.created
 *   - Consumer group: wallet-service-consumer-group
 * 
 * - Produces 'wallet.balance.updated' events after any balance change
 *   - Published after credit or debit operations
 *   - Topic: wallet.balance.updated
 *   - Event contains: walletId, userId, previousBalance, newBalance, changeAmount, currency, transactionType
 * 
 * Error Handling:
 * - WalletNotFoundException (404): Wallet not found for user
 * - InsufficientBalanceException (400): Insufficient balance for debit operation
 * - Validation errors (400): Invalid request parameters
 * - Global exception handler with proper error responses
 * 
 * Configuration:
 * - Port: 8083 (default)
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
 * - Container exposes port 8083
 * - Requires PostgreSQL and Kafka to be accessible
 * - Environment variables for database and Kafka configuration
 * - Health check endpoint configured
 * 
 * Service Discovery:
 * - Registers with Eureka server
 * - Discoverable as 'wallet-service'
 * 
 * Security Considerations:
 * - In production, add authentication/authorization
 * - Validate user ownership before wallet operations
 * - Implement rate limiting for API endpoints
 * - Add audit logging for all balance changes
 * 
 * Performance:
 * - Pessimistic locking for balance updates prevents race conditions
 * - Optimistic locking for read operations
 * - Connection pooling via HikariCP
 * - Kafka producer with idempotence enabled
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class WalletServiceApplication {

    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        DotEnvConfig.loadDotEnv();
        
        SpringApplication.run(WalletServiceApplication.class, args);
    }
}





