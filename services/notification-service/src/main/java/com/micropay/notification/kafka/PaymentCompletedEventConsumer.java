package com.micropay.notification.kafka;

import com.micropay.notification.dto.PaymentCompletedEvent;
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
 * Kafka consumer for payment.completed events
 * Sends notifications when payments are completed
 */
@Component
public class PaymentCompletedEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompletedEventConsumer.class);

    private final NotificationService notificationService;

    public PaymentCompletedEventConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Consume payment.completed events from Kafka
     * Sends notifications to payer and payee
     */
    @KafkaListener(topics = "payment.completed", groupId = "notification-service-consumer-group")
    public void consumePaymentCompletedEvent(
            @Payload PaymentCompletedEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("Received payment.completed event for payment: {}", event.getPaymentId());
            
            if (event.getPaymentId() == null) {
                logger.error("Payment ID is null in payment.completed event. Event ID: {}", event.getEventId());
                acknowledgment.acknowledge();
                return;
            }

            // Send notifications for payment completion
            notificationService.sendPaymentCompletedNotification(event);
            logger.debug("Processed payment.completed event for payment: {}", event.getPaymentId());

            // Acknowledge the message
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            logger.error("Error processing payment.completed event for payment: {}. Event ID: {}", 
                        event.getPaymentId(), event.getEventId(), e);
            // In production, implement retry logic or send to dead letter queue
            // For now, acknowledge to prevent infinite retries
            acknowledgment.acknowledge();
        }
    }
}




