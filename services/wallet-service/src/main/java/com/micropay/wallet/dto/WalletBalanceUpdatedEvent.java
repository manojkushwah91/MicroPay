package com.micropay.wallet.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Kafka event DTO for wallet.balance.updated event
 */
public class WalletBalanceUpdatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID walletId;
    private UUID userId;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private BigDecimal changeAmount;
    private String currency;
    private String transactionType; // CREDIT or DEBIT
    private String transactionId;
    private LocalDateTime updatedAt;

    // Constructors
    public WalletBalanceUpdatedEvent() {
    }

    public WalletBalanceUpdatedEvent(UUID walletId, UUID userId, BigDecimal previousBalance, 
                                     BigDecimal newBalance, BigDecimal changeAmount, 
                                     String currency, String transactionType, String transactionId) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "wallet.balance.updated";
        this.timestamp = LocalDateTime.now();
        this.walletId = walletId;
        this.userId = userId;
        this.previousBalance = previousBalance;
        this.newBalance = newBalance;
        this.changeAmount = changeAmount;
        this.currency = currency;
        this.transactionType = transactionType;
        this.transactionId = transactionId;
        this.updatedAt = LocalDateTime.now();
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

    public UUID getWalletId() {
        return walletId;
    }

    public void setWalletId(UUID walletId) {
        this.walletId = walletId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void setPreviousBalance(BigDecimal previousBalance) {
        this.previousBalance = previousBalance;
    }

    public BigDecimal getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(BigDecimal newBalance) {
        this.newBalance = newBalance;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}




