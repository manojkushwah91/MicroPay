package com.micropay.payment.kafka;

import com.micropay.payment.dto.WalletBalanceUpdatedEvent;
import com.micropay.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for wallet.balance.updated events
 * Processes pending payments when wallet balance is updated
 */
@Component
public class WalletBalanceUpdatedEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(WalletBalanceUpdatedEventConsumer.class);

    private final PaymentService paymentService;

    public WalletBalanceUpdatedEventConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Consume wallet.balance.updated events from Kafka
     * Checks for sufficient balance and triggers payment processing
     */
    @KafkaListener(topics = "wallet.balance.updated", groupId = "payment-service-consumer-group")
    public void consumeWalletBalanceUpdatedEvent(
            @Payload WalletBalanceUpdatedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received wallet.balance.updated event for user: {}, new balance: {}", 
                      event.getUserId(), event.getNewBalance());
            
            if (event.getUserId() == null) {
                logger.error("User ID is null in wallet.balance.updated event. Event ID: {}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // Process pending payments for this user
            paymentService.processPaymentOnBalanceUpdate(event);
            logger.debug("Processed payments for user: {} after balance update", event.getUserId());

            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing wallet.balance.updated event for user: {}. Event ID: {}", 
                        event.getUserId(), event.getEventId(), e);
            // In production, implement retry logic or send to dead letter queue
            // For now, acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}




