package com.micropay.transaction.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration class to load environment variables from .env file
 * This ensures .env variables are available to Spring Boot's property resolution
 */
public class DotEnvConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DotEnvConfig.class);
    private static Dotenv dotenv;
    
    /**
     * Loads .env file and sets variables as system properties
     * This should be called before SpringApplication.run()
     */
    public static void loadDotEnv() {
        try {
            dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();
            
            // Set all .env variables as system properties for Spring property resolution
            dotenv.entries().forEach(entry -> {
                // Only set if not already set (system/env vars take precedence)
                if (System.getProperty(entry.getKey()) == null && System.getenv(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
            
            // Validate required database password
            String dbPassword = getDbPassword();
            if (dbPassword == null || dbPassword.trim().isEmpty()) {
                logger.error("================================================");
                logger.error("CRITICAL: DB_PASSWORD is missing from .env file!");
                logger.error("Please create a .env file in the project root with:");
                logger.error("DB_PASSWORD=your_database_password");
                logger.error("================================================");
                throw new IllegalStateException(
                    "DB_PASSWORD environment variable is required but not found in .env file. " +
                    "Please create a .env file in the project root with DB_PASSWORD set."
                );
            }
            
            logger.info("Successfully loaded environment variables from .env file");
            logger.debug("DB_PASSWORD is configured (length: {})", dbPassword.length());
            
        } catch (IllegalStateException e) {
            // Re-throw validation errors
            throw e;
        } catch (Exception e) {
            logger.warn("Could not load .env file: {}. Using system environment variables instead.", e.getMessage());
        }
    }
    
    /**
     * Gets DB_PASSWORD from .env file or system environment
     */
    private static String getDbPassword() {
        // Check system property first (set from .env)
        String password = System.getProperty("DB_PASSWORD");
        if (password != null && !password.trim().isEmpty()) {
            return password;
        }
        
        // Check system environment variable
        password = System.getenv("DB_PASSWORD");
        if (password != null && !password.trim().isEmpty()) {
            return password;
        }
        
        // Check .env file
        if (dotenv != null) {
            password = dotenv.get("DB_PASSWORD");
            if (password != null && !password.trim().isEmpty()) {
                return password;
            }
        }
        
        return null;
    }
}



