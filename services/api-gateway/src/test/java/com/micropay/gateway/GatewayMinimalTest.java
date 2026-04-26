package com.micropay.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("API Gateway Minimal Tests")
class GatewayMinimalTest {

    @Test
    @DisplayName("Gateway application context should load")
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads successfully
        // with JWT secret properly configured
    }
}
