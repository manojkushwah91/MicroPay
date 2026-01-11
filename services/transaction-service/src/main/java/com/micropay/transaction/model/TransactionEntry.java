package com.micropay.transaction.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Transaction entry representing a single entry in double-entry bookkeeping
 */
@Entity
@Table(name = "transaction_entries", indexes = {
    @Index(name = "idx_entry_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_entry_user_id", columnList = "user_id")
})
public class TransactionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "entry_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TransactionEntryType entryType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    // Constructors
    public TransactionEntry() {
    }

    public TransactionEntry(UUID userId, TransactionEntryType entryType, BigDecimal amount, String currency) {
        this.userId = userId;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TransactionEntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(TransactionEntryType entryType) {
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




