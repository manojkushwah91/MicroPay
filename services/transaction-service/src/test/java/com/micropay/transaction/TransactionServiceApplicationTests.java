package com.micropay.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.config.import=",
    "spring.config.location=classpath:application-test.yml",
    "jwt.secret=test-secret-key-for-testing-only-long-enough-to-be-secure",
    "jwt.expiration=86400000",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class TransactionServiceApplicationTests {

    @Test
    void contextLoads() {
        // Test that Spring context loads successfully
    }
}







