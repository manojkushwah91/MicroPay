package com.micropay.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event DTO for payment.failed event
 */
public class PaymentFailedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID paymentId;
    private UUID payerUserId;
    private UUID payeeUserId;
    private BigDecimal amount;
    private String currency;
    private String failureReason;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime failedAt;
    private Map<String, Object> metadata;

    // Constructors
    public PaymentFailedEvent() {
    }

    public PaymentFailedEvent(UUID paymentId, UUID payerUserId, UUID payeeUserId, 
                             BigDecimal amount, String currency, String failureReason, 
                             String errorCode, String errorMessage) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "payment.failed";
        this.timestamp = LocalDateTime.now();
        this.paymentId = paymentId;
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.currency = currency;
        this.failureReason = failureReason;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.failedAt = LocalDateTime.now();
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

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}




