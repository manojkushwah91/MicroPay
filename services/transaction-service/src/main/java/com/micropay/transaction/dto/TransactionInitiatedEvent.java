package com.micropay.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionInitiatedEvent {
    
    private UUID eventId;
    private String eventType = "transaction.initiated";
    private LocalDateTime timestamp;
    private UUID transactionId;
    private UUID fromUserId;
    private UUID toUserId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    
    public TransactionInitiatedEvent() {
        this.timestamp = LocalDateTime.now();
        this.eventId = UUID.randomUUID();
    }
    
    public TransactionInitiatedEvent(UUID transactionId, UUID fromUserId, UUID toUserId, 
                                   BigDecimal amount, String currency, String description, String status) {
        this();
        this.transactionId = transactionId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.status = status;
    }
    
    public UUID getEventId() {
        return eventId;
    }
    
    public void setEventId(UUID eventId) {
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
    
    public UUID getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
    
    public UUID getFromUserId() {
        return fromUserId;
    }
    
    public void setFromUserId(UUID fromUserId) {
        this.fromUserId = fromUserId;
    }
    
    public UUID getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(UUID toUserId) {
        this.toUserId = toUserId;
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
