package com.micropay.events.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentRefundedEvent {
    private UUID paymentId;
    private UUID originalPaymentId;
    private UUID payerUserId;
    private UUID payeeUserId;
    private BigDecimal amount;
    private String currency;

    public PaymentRefundedEvent() {
    }

    public PaymentRefundedEvent(UUID paymentId, UUID originalPaymentId, UUID payerUserId, UUID payeeUserId, BigDecimal amount, String currency) {
        this.paymentId = paymentId;
        this.originalPaymentId = originalPaymentId;
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.currency = currency;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

    public UUID getOriginalPaymentId() {
        return originalPaymentId;
    }

    public void setOriginalPaymentId(UUID originalPaymentId) {
        this.originalPaymentId = originalPaymentId;
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
}
