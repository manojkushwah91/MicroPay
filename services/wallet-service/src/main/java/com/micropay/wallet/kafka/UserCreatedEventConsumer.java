package com.micropay.wallet.kafka;

import com.micropay.wallet.dto.UserCreatedEvent;
import com.micropay.wallet.service.WalletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka consumer for user.created events
 * Creates a wallet automatically when a new user is registered
 */
@Component
public class UserCreatedEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserCreatedEventConsumer.class);

    private final WalletService walletService;

    public UserCreatedEventConsumer(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Consume user.created events from Kafka
     * Creates a wallet for the new user
     */
    @KafkaListener(topics = "user.created", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserCreatedEvent(
            @Payload UserCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received user.created event for user: {}", event.getUserId());
            
            if (event.getUserId() == null) {
                logger.error("User ID is null in user.created event. Event ID: {}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // Create wallet for the new user
            walletService.createWallet(event.getUserId(), "USD");
            logger.info("Successfully created wallet for user: {}", event.getUserId());

            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing user.created event for user: {}. Event ID: {}", 
                        event.getUserId(), event.getEventId(), e);
            // In production, implement retry logic or send to dead letter queue
            // For now, acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}

