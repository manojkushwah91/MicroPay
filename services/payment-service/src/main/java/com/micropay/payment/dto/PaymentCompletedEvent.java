package com.micropay.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event DTO for payment.completed event
 */
public class PaymentCompletedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID paymentId;
    private UUID payerUserId;
    private UUID payeeUserId;
    private BigDecimal amount;
    private String currency;
    private UUID transactionId;
    private LocalDateTime completedAt;
    private Map<String, Object> metadata;

    // Constructors
    public PaymentCompletedEvent() {
    }

    public PaymentCompletedEvent(UUID paymentId, UUID payerUserId, UUID payeeUserId, 
                                 BigDecimal amount, String currency, UUID transactionId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "payment.completed";
        this.timestamp = LocalDateTime.now();
        this.paymentId = paymentId;
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.currency = currency;
        this.transactionId = transactionId;
        this.completedAt = LocalDateTime.now();
        this.metadata = new HashMap<>();
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

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getPayerUserId() {
        return payerUserId;
    }

    public void setPayerUserId(UUID payerUserId) {
        this.payerUserId = payerUserId;
    }

    public UUID getPayeeUserId() {
        return payeeUserId;
    }

    public void setPayeeUserId(UUID payeeUserId) {
        this.payeeUserId = payeeUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}




