package com.micropay.gateway;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public com.micropay.gateway.util.JwtUtil jwtUtil() {
        com.micropay.gateway.util.JwtUtil jwtUtil = new com.micropay.gateway.util.JwtUtil();
        // Set a test secret using reflection or create a test implementation
        return jwtUtil;
    }
}
