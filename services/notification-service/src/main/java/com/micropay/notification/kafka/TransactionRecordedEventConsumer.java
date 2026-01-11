package com.micropay.notification.kafka;

import com.micropay.notification.dto.TransactionRecordedEvent;
import com.micropay.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for transaction.recorded events
 * Sends notifications when transactions are recorded
 */
@Component
public class TransactionRecordedEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TransactionRecordedEventConsumer.class);

    private final NotificationService notificationService;

    public TransactionRecordedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Consume transaction.recorded events from Kafka
     * Sends notifications to all users involved in the transaction
     */
    @KafkaListener(topics = "transaction.recorded", groupId = "notification-service-consumer-group")
    public void consumeTransactionRecordedEvent(
            @Payload TransactionRecordedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received transaction.recorded event for transaction: {}", event.getTransactionId());
            
            if (event.getTransactionId() == null) {
                logger.error("Transaction ID is null in transaction.recorded event. Event ID: {}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // Send notifications for transaction recording
            notificationService.sendTransactionRecordedNotification(event);
            logger.debug("Processed transaction.recorded event for transaction: {}", event.getTransactionId());

            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing transaction.recorded event for transaction: {}. Event ID: {}", 
                        event.getTransactionId(), event.getEventId(), e);
            // In production, implement retry logic or send to dead letter queue
            // For now, acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}




