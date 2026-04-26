package com.micropay.auth;

import com.micropay.auth.service.AuthService;
import com.micropay.auth.service.JwtService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(
    basePackages = "com.micropay.auth",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {AuthService.class, JwtService.class}
    )
)
public class TestConfig {

    @Bean
    @Primary
    public AuthService authService() {
        return mock(AuthService.class);
    }

    @Bean
    @Primary
    public JwtService jwtService() {
        return mock(JwtService.class);
    }
}
