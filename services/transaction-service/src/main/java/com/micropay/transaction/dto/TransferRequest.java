package com.micropay.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class TransferRequest {
    
    @NotNull(message = "From user ID is required")
    private UUID fromUserId;
    
    @NotNull(message = "To user ID is required")
    private UUID toUserId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency = "USD";
    
    @NotBlank(message = "Description is required")
    private String description;
    
    public TransferRequest() {}
    
    public TransferRequest(UUID fromUserId, UUID toUserId, BigDecimal amount, String currency, String description) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
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
}
