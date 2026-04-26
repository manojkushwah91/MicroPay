package com.micropay.wallet;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        ProducerFactory<String, Object> producerFactory = mock(ProducerFactory.class);
        return new KafkaTemplate<>(producerFactory);
    }
}
