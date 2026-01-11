package com.micropay.payment.repository;

import com.micropay.payment.model.Payment;
import com.micropay.payment.model.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Payment entity
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    /**
     * Find payment by paymentId (for idempotency)
     */
    Optional<Payment> findByPaymentId(UUID paymentId);

    /**
     * Find payment by idempotency key (for idempotency check)
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find payments by payer user ID
     */
    List<Payment> findByPayerUserId(UUID payerUserId);

    /**
     * Find payments by payee user ID
     */
    List<Payment> findByPayeeUserId(UUID payeeUserId);

    /**
     * Find payments by status
     */
    List<Payment> findByStatus(PaymentStatus status);

    /**
     * Find pending payments for a user (waiting for balance update)
     */
    @Query("SELECT p FROM Payment p WHERE p.payerUserId = :userId AND p.status = :status")
    List<Payment> findPendingPaymentsByUserId(UUID userId, PaymentStatus status);
}




