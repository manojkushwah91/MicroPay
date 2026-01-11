package com.micropay.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Kafka event DTO for transaction.recorded event
 */
public class TransactionRecordedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID transactionId;
    private UUID paymentId;
    private List<TransactionEntryDto> entries;
    private String status;
    private LocalDateTime recordedAt;

    // Constructors
    public TransactionRecordedEvent() {
    }

    public TransactionRecordedEvent(UUID transactionId, UUID paymentId, List<TransactionEntryDto> entries) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "transaction.recorded";
        this.timestamp = LocalDateTime.now();
        this.transactionId = transactionId;
        this.paymentId = paymentId;
        this.entries = entries != null ? entries : new ArrayList<>();
        this.status = "RECORDED";
        this.recordedAt = LocalDateTime.now();
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

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public List<TransactionEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<TransactionEntryDto> entries) {
        this.entries = entries;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    /**
     * Transaction entry DTO for event
     */
    public static class TransactionEntryDto {
        private UUID userId;
        private String entryType; // DEBIT or CREDIT
        private BigDecimal amount;
        private String currency;

        public TransactionEntryDto() {
        }

        public TransactionEntryDto(UUID userId, String entryType, BigDecimal amount, String currency) {
            this.userId = userId;
            this.entryType = entryType;
            this.amount = amount;
            this.currency = currency;
        }

        // Getters and Setters
        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public String getEntryType() {
            return entryType;
        }

        public void setEntryType(String entryType) {
            this.entryType = entryType;
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
    }
}




