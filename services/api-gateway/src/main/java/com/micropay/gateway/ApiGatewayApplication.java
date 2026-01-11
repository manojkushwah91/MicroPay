package com.micropay.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MicroPay API Gateway Application
 * 
 * README:
 * =======
 * This is the API Gateway for all MicroPay microservices.
 * 
 * Purpose:
 * - Single entry point for all external requests
 * - Routes requests to appropriate microservices via Eureka
 * - Provides load balancing across service instances
 * - Handles authentication and authorization (JWT placeholder)
 * - Implements rate limiting and circuit breakers
 * - Centralizes cross-cutting concerns (logging, monitoring)
 * 
 * Usage:
 * - Gateway URL: http://localhost:8080
 * - Health Check: http://localhost:8080/actuator/health
 * - Service Routes: http://localhost:8080/api/v1/{service-path}
 * 
 * Routing:
 * - User Service: /api/v1/users/** -> lb://user-service
 * - Account Service: /api/v1/accounts/** -> lb://account-service
 * - Payment Service: /api/v1/payments/** -> lb://payment-service
 * - Transaction Service: /api/v1/transactions/** -> lb://transaction-service
 * - Reporting Service: /api/v1/reports/** -> lb://reporting-service
 * 
 * Load Balancing:
 * - Uses Eureka service discovery for service instance lookup
 * - Implements client-side load balancing (Ribbon)
 * - Routes to healthy service instances only
 * 
 * Security:
 * - Spring Security enabled (JWT authentication placeholder)
 * - Public endpoints: /api/v1/users/register, /api/v1/users/login
 * - Protected endpoints: All other /api/v1/** routes
 * - JWT validation will be implemented in future iterations
 * 
 * Configuration:
 * - Port: 8080 (default)
 * - Eureka Client: Enabled (registers with Eureka server)
 * - Service Discovery: Enabled (discovers services via Eureka)
 * 
 * Actuator Endpoints:
 * - /actuator/health - Gateway health status
 * - /actuator/info - Gateway information
 * - /actuator/gateway/routes - List all configured routes
 * - /actuator/gateway/refresh - Refresh route configuration
 * 
 * Docker:
 * - Container exposes port 8080
 * - Requires Eureka server to be accessible
 * - Environment: EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka
 * 
 * Future Enhancements:
 * - JWT token validation
 * - Rate limiting (Redis-based)
 * - Request/response transformation
 * - API versioning
 * - Circuit breakers (Resilience4j)
 * - Distributed tracing
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}

