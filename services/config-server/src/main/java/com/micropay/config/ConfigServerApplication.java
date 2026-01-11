package com.micropay.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * MicroPay Config Server Application
 * 
 * README:
 * =======
 * This is the centralized configuration server for all MicroPay microservices.
 * 
 * Purpose:
 * - Provides centralized configuration management for all microservices
 * - Supports environment-specific configurations (dev, staging, prod)
 * - Enables dynamic configuration refresh without service restarts
 * - Stores configuration in local filesystem (native profile)
 * 
 * Usage:
 * - Service URL: http://localhost:8888
 * - Health Check: http://localhost:8888/actuator/health
 * - Config Endpoint: http://localhost:8888/{application}/{profile}
 * 
 * Configuration Location:
 * - Native profile uses local filesystem: ./config-repo
 * - Each service has its own configuration file: {service-name}.yml
 * - Environment-specific: {service-name}-{profile}.yml
 * 
 * Example:
 * - User Service (dev): http://localhost:8888/user-service/dev
 * - Payment Service (prod): http://localhost:8888/payment-service/prod
 * 
 * Actuator Endpoints:
 * - /actuator/health - Service health status
 * - /actuator/info - Service information
 * 
 * Docker:
 * - Container exposes port 8888
 * - Mount config-repo directory as volume
 * - Environment: SPRING_PROFILES_ACTIVE=native
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}

