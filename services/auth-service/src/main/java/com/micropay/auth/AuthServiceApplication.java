package com.micropay.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * MicroPay Authentication Service Application
 * 
 * README:
 * =======
 * This is the authentication and authorization service for MicroPay platform.
 * 
 * Purpose:
 * - User registration and account creation
 * - User authentication (login) with JWT tokens
 * - JWT token generation and validation
 * - Role-based access control (RBAC)
 * - User management and profile handling
 * 
 * Features:
 * - RESTful API for registration and login
 * - JWT-based authentication (stateless)
 * - Password encryption using BCrypt
 * - PostgreSQL database for user storage
 * - Kafka event publishing (user.created on registration)
 * - Spring Security integration
 * - OAuth2 ready (prepared for future OAuth2 integration)
 * 
 * API Endpoints:
 * - POST /auth/register - Register a new user
 *   Request: { "email": "user@example.com", "password": "password123", "firstName": "John", "lastName": "Doe" }
 *   Response: { "token": "jwt-token", "userId": "uuid", "email": "user@example.com" }
 * 
 * - POST /auth/login - Authenticate user and get JWT token
 *   Request: { "email": "user@example.com", "password": "password123" }
 *   Response: { "token": "jwt-token", "userId": "uuid", "email": "user@example.com" }
 * 
 * Database:
 * - PostgreSQL database: micropay_auth_db
 * - Tables: users, roles, user_roles
 * - Flyway migrations for schema management
 * 
 * Kafka Integration:
 * - Publishes 'user.created' event on successful registration
 * - Event contains: userId, email, firstName, lastName, createdAt
 * 
 * Security:
 * - JWT tokens with configurable expiration
 * - Password hashing with BCrypt
 * - Role-based authorization
 * - CORS configuration
 * - CSRF protection (disabled for API endpoints)
 * 
 * Configuration:
 * - Port: 8081 (default)
 * - JWT Secret: Configured via application.yml or environment variable
 * - JWT Expiration: 24 hours (configurable)
 * - Database: PostgreSQL connection via Spring Cloud Config
 * - Kafka: Bootstrap servers via Spring Cloud Config
 * 
 * Actuator Endpoints:
 * - /actuator/health - Service health status
 * - /actuator/info - Service information
 * 
 * Docker:
 * - Container exposes port 8081
 * - Requires PostgreSQL and Kafka to be accessible
 * - Environment variables for database and Kafka configuration
 * 
 * Service Discovery:
 * - Registers with Eureka server
 * - Discoverable as 'auth-service'
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

