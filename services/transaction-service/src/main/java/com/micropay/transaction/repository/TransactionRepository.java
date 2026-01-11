package com.micropay.transaction.repository;

import com.micropay.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    /**
     * Find transaction by transactionId
     */
    Optional<Transaction> findByTransactionId(UUID transactionId);

    /**
     * Find transactions by paymentId
     */
    Optional<Transaction> findByPaymentId(UUID paymentId);

    /**
     * Find all transactions for a user (payer or payee)
     */
    @Query("SELECT DISTINCT t FROM Transaction t " +
           "JOIN t.entries e " +
           "WHERE e.userId = :userId " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findByUserId(UUID userId);

    /**
     * Find transactions by status
     */
    List<Transaction> findByStatus(com.micropay.transaction.model.TransactionStatus status);
}




