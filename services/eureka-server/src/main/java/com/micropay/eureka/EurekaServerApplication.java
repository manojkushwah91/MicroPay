package com.micropay.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * MicroPay Eureka Discovery Server Application
 * 
 * README:
 * =======
 * This is the service discovery server for all MicroPay microservices.
 * 
 * Purpose:
 * - Provides service registration and discovery for all microservices
 * - Enables load balancing across service instances
 * - Tracks service health and availability
 * - Provides service registry dashboard
 * 
 * Usage:
 * - Server URL: http://localhost:8761
 * - Dashboard: http://localhost:8761 (Eureka UI)
 * - Health Check: http://localhost:8761/actuator/health
 * - Service Registry: http://localhost:8761/eureka/apps
 * 
 * Service Registration:
 * - Microservices register themselves with Eureka on startup
 * - Services send heartbeats every 30 seconds (default)
 * - Services are removed from registry after 90 seconds of no heartbeat
 * 
 * Configuration:
 * - Port: 8761 (default)
 * - Self-preservation mode: Enabled (prevents registry cleanup in case of network issues)
 * - Renewal threshold: 85% of registered services must renew within 15 minutes
 * 
 * Actuator Endpoints:
 * - /actuator/health - Service health status
 * - /actuator/info - Service information
 * 
 * Docker:
 * - Container exposes port 8761
 * - Environment: SPRING_PROFILES_ACTIVE=default
 * - No external dependencies required
 * 
 * High Availability:
 * - For production, deploy multiple Eureka instances
 * - Configure peer-to-peer replication between instances
 * - Each instance registers with other instances
 * 
 * @author MicroPay Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

