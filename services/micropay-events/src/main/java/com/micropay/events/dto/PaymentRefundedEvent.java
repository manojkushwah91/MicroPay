package com.micropay.events.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentRefundedEvent {

    private UUID paymentId;
    private UUID orderId;
    private UUID transactionId;
    private UUID userId;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime refundedAt;

    // Default constructor for serialization
    public PaymentRefundedEvent() {}

    // Original constructor (kept for compatibility)
    public PaymentRefundedEvent(UUID paymentId, double amount, String currency, UUID userId, LocalDateTime refundedAt) {
        this.paymentId = paymentId;
        this.amount = BigDecimal.valueOf(amount);
        this.currency = currency;
        this.userId = userId;
        this.refundedAt = refundedAt;
    }

    // New constructor (matches what PaymentService is calling)
    public PaymentRefundedEvent(
            UUID paymentId,
            UUID orderId,
            UUID transactionId,
            UUID userId,
            BigDecimal amount,
            String currency
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.transactionId = transactionId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.refundedAt = LocalDateTime.now();
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public LocalDateTime getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(LocalDateTime refundedAt) {
        this.refundedAt = refundedAt;
    }

    @Override
    public String toString() {
        return "PaymentRefundedEvent{" +
                "paymentId=" + paymentId +
                ", orderId=" + orderId +
                ", transactionId=" + transactionId +
                ", userId=" + userId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", refundedAt=" + refundedAt +
                '}';
    }
}