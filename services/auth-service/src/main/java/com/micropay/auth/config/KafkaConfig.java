package com.micropay.auth.config;

import com.micropay.events.dto.UserCreatedEvent;
import com.micropay.events.dto.PasswordResetEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka producer configuration
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // --- 1. Beans for UserCreatedEvent (Existing) ---
    @Bean
    public ProducerFactory<String, UserCreatedEvent> userCreatedProducerFactory() {
        return new DefaultKafkaProducerFactory<>(getBasicConfig());
    }

    @Bean
    public KafkaTemplate<String, UserCreatedEvent> kafkaTemplate() {
        return new KafkaTemplate<>(userCreatedProducerFactory());
    }

    // --- 2. Beans for PasswordResetEvent (MISSING PIECE) ---
    @Bean
    public ProducerFactory<String, PasswordResetEvent> passwordResetProducerFactory() {
        return new DefaultKafkaProducerFactory<>(getBasicConfig());
    }

    @Bean
    public KafkaTemplate<String, PasswordResetEvent> passwordResetKafkaTemplate() {
        return new KafkaTemplate<>(passwordResetProducerFactory());
    }

    // Helper method to keep your code clean
    private Map<String, Object> getBasicConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return configProps;
    }
}

