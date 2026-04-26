package com.micropay.payment.dto;

import java.math.BigDecimal;

public class VerifyFundsResponse {
    
    private boolean sufficient;
    private BigDecimal availableBalance;
    private BigDecimal requestedAmount;
    private String currency;
    
    public VerifyFundsResponse() {}
    
    public VerifyFundsResponse(boolean sufficient, BigDecimal availableBalance, BigDecimal requestedAmount, String currency) {
        this.sufficient = sufficient;
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
        this.currency = currency;
    }
    
    public boolean isSufficient() {
        return sufficient;
    }
    
    public void setSufficient(boolean sufficient) {
        this.sufficient = sufficient;
    }
    
    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
    
    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }
    
    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
    
    public void setRequestedAmount(BigDecimal requestedAmount) {
        this.requestedAmount = requestedAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
