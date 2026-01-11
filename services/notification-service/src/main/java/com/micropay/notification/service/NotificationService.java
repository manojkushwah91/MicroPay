package com.micropay.notification.service;

import com.micropay.notification.dto.NotificationSendEvent;
import com.micropay.notification.dto.NotificationResponse;
import com.micropay.notification.dto.PaymentCompletedEvent;
import com.micropay.notification.dto.TransactionRecordedEvent;
import com.micropay.notification.model.Notification;
import com.micropay.notification.model.NotificationChannel;
import com.micropay.notification.model.NotificationStatus;
import com.micropay.notification.model.NotificationType;
import com.micropay.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for notification operations
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    private static final String NOTIFICATION_SEND_TOPIC = "notification.send";

    private final NotificationRepository notificationRepository;
    private final KafkaTemplate<String, NotificationSendEvent> kafkaTemplate;

    public NotificationService(NotificationRepository notificationRepository, 
                              KafkaTemplate<String, NotificationSendEvent> kafkaTemplate) {
        this.notificationRepository = notificationRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send notification for payment completed event
     */
    @Transactional
    public void sendPaymentCompletedNotification(PaymentCompletedEvent event) {
        try {
            // Send notification to payer
            sendNotification(
                event.getPayerUserId(),
                NotificationType.PAYMENT_COMPLETED,
                NotificationChannel.IN_APP,
                "Payment Completed",
                String.format("Your payment of %s %s has been completed successfully. Payment ID: %s", 
                            event.getAmount(), event.getCurrency(), event.getPaymentId()),
                event.getPaymentId(),
                "PAYMENT"
            );

            // Send notification to payee if exists
            if (event.getPayeeUserId() != null) {
                sendNotification(
                    event.getPayeeUserId(),
                    NotificationType.PAYMENT_COMPLETED,
                    NotificationChannel.IN_APP,
                    "Payment Received",
                    String.format("You have received %s %s. Payment ID: %s", 
                                event.getAmount(), event.getCurrency(), event.getPaymentId()),
                    event.getPaymentId(),
                    "PAYMENT"
                );
            }

            logger.info("Sent payment completed notifications for payment: {}", event.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to send payment completed notification for payment: {}", 
                        event.getPaymentId(), e);
        }
    }

    /**
     * Send notification for transaction recorded event
     */
    @Transactional
    public void sendTransactionRecordedNotification(TransactionRecordedEvent event) {
        try {
            if (event.getEntries() == null || event.getEntries().isEmpty()) {
                logger.warn("Transaction recorded event has no entries: {}", event.getTransactionId());
                return;
            }

            // Send notification to all users involved in the transaction
            for (TransactionRecordedEvent.TransactionEntryDto entry : event.getEntries()) {
                String entryTypeLabel = "DEBIT".equals(entry.getEntryType()) ? "debited" : "credited";
                sendNotification(
                    entry.getUserId(),
                    NotificationType.TRANSACTION_RECORDED,
                    NotificationChannel.IN_APP,
                    "Transaction Recorded",
                    String.format("Your account has been %s with %s %s. Transaction ID: %s", 
                                entryTypeLabel, entry.getAmount(), entry.getCurrency(), 
                                event.getTransactionId()),
                    event.getTransactionId(),
                    "TRANSACTION"
                );
            }

            logger.info("Sent transaction recorded notifications for transaction: {}", event.getTransactionId());
        } catch (Exception e) {
            logger.error("Failed to send transaction recorded notification for transaction: {}", 
                        event.getTransactionId(), e);
        }
    }

    /**
     * Send notification (placeholder implementation)
     */
    @Transactional
    public Notification sendNotification(UUID userId, NotificationType notificationType, 
                                        NotificationChannel channel, String title, String message,
                                        UUID referenceId, String referenceType) {
        // Create notification record
        Notification notification = new Notification(userId, notificationType, channel, title, message);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification = notificationRepository.save(notification);

        // Placeholder: Simulate sending notification
        // In production, this would integrate with email/SMS/push notification services
        try {
            // Simulate notification sending (placeholder logic)
            simulateNotificationSending(notification);
            
            // Mark as sent
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(java.time.LocalDateTime.now());
            notification = notificationRepository.save(notification);

            logger.info("Notification sent successfully: {} to user: {}", notification.getId(), userId);

            // Publish notification.send event
            publishNotificationSendEvent(notification);

        } catch (Exception e) {
            logger.error("Failed to send notification: {}", notification.getId(), e);
            notification.setStatus(NotificationStatus.FAILED);
            notification.setFailedAt(java.time.LocalDateTime.now());
            notification.setFailureReason("Failed to send: " + e.getMessage());
            notification = notificationRepository.save(notification);
        }

        return notification;
    }

    /**
     * Placeholder method to simulate notification sending
     * In production, this would integrate with actual notification services
     */
    private void simulateNotificationSending(Notification notification) {
        // Placeholder logic - simulate sending delay
        try {
            Thread.sleep(50); // Simulate network delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Notification sending interrupted", e);
        }

        // Placeholder: Log notification details
        logger.debug("Sending notification via {} to user {}: {}", 
                    notification.getChannel(), notification.getUserId(), notification.getTitle());
        
        // In production, this would:
        // - For EMAIL: Call email service (SendGrid, AWS SES, etc.)
        // - For SMS: Call SMS service (Twilio, AWS SNS, etc.)
        // - For PUSH: Call push notification service (FCM, APNS, etc.)
        // - For IN_APP: Store in user's notification inbox
    }

    /**
     * Get notifications for a user
     */
    public List<NotificationResponse> getNotificationsByUserId(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Publish notification.send event to Kafka
     */
    private void publishNotificationSendEvent(Notification notification) {
        try {
            NotificationSendEvent event = new NotificationSendEvent(
                notification.getId(),
                notification.getUserId(),
                notification.getNotificationType().name(),
                notification.getChannel().name(),
                notification.getStatus().name(),
                notification.getReferenceId(),
                notification.getReferenceType()
            );

            kafkaTemplate.send(NOTIFICATION_SEND_TOPIC, notification.getUserId().toString(), event);
            logger.debug("Published notification.send event for notification: {}", notification.getId());
        } catch (Exception e) {
            logger.error("Failed to publish notification.send event for notification: {}", 
                        notification.getId(), e);
            // Note: In production, consider implementing retry mechanism or dead letter queue
        }
    }

    /**
     * Map Notification entity to NotificationResponse DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setUserId(notification.getUserId());
        response.setNotificationType(notification.getNotificationType().name());
        response.setChannel(notification.getChannel().name());
        response.setStatus(notification.getStatus().name());
        response.setTitle(notification.getTitle());
        response.setMessage(notification.getMessage());
        response.setReferenceId(notification.getReferenceId());
        response.setReferenceType(notification.getReferenceType());
        response.setCreatedAt(notification.getCreatedAt());
        response.setSentAt(notification.getSentAt());
        response.setFailedAt(notification.getFailedAt());
        response.setFailureReason(notification.getFailureReason());
        return response;
    }
}




