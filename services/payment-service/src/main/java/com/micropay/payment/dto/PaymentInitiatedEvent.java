package com.micropay.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event DTO for payment.initiated event
 */
public class PaymentInitiatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID paymentId;
    private String idempotencyKey;
    private UUID payerUserId;
    private UUID payeeUserId;
    private BigDecimal amount;
    private String currency;
    private String paymentType;
    private String status;
    private Map<String, Object> metadata;

    // Constructors
    public PaymentInitiatedEvent() {
    }

    public PaymentInitiatedEvent(UUID paymentId, String idempotencyKey, UUID payerUserId, 
                                UUID payeeUserId, BigDecimal amount, String currency, 
                                String paymentType, String description, String reference, 
                                UUID initiatedBy) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "payment.initiated";
        this.timestamp = LocalDateTime.now();
        this.paymentId = paymentId;
        this.idempotencyKey = idempotencyKey;
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
        this.status = "INITIATED";
        this.metadata = new HashMap<>();
        if (description != null) {
            this.metadata.put("description", description);
        }
        if (reference != null) {
            this.metadata.put("reference", reference);
        }
        if (initiatedBy != null) {
            this.metadata.put("initiatedBy", initiatedBy.toString());
        }
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}







