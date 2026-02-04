package com.micropay.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event DTO for notification.send event
 */
public class NotificationSendEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID notificationId;
    private UUID userId;
    private String notificationType;
    private String channel;
    private String status;
    private UUID referenceId;
    private String referenceType;
    private LocalDateTime sentAt;

    // Constructors
    public NotificationSendEvent() {
    }

    public NotificationSendEvent(UUID notificationId, UUID userId, String notificationType, 
                                String channel, String status, UUID referenceId, String referenceType) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "notification.send";
        this.timestamp = LocalDateTime.now();
        this.notificationId = notificationId;
        this.userId = userId;
        this.notificationType = notificationType;
        this.channel = channel;
        this.status = status;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(UUID notificationId) {
        this.notificationId = notificationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(UUID referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}







