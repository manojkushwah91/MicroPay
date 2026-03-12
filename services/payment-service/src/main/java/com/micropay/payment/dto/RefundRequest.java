package com.micropay.payment.dto;

import java.math.BigDecimal;

public class RefundRequest {
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
