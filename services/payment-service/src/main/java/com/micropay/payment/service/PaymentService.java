package com.micropay.payment.service;

import com.micropay.payment.dto.*;
import com.micropay.payment.exception.DuplicatePaymentException;
import com.micropay.payment.exception.PaymentNotFoundException;
import com.micropay.payment.model.Payment;
import com.micropay.payment.model.PaymentStatus;
import com.micropay.payment.model.PaymentType;
import com.micropay.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for payment operations
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    private static final String PAYMENT_INITIATED_TOPIC = "payment.initiated";
    private static final String PAYMENT_AUTHORIZED_TOPIC = "payment.authorized";
    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository, 
                        KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Initiate a payment with idempotency check
     */
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        // Check for duplicate payment using idempotency key
        paymentRepository.findByIdempotencyKey(request.getIdempotencyKey())
            .ifPresent(existingPayment -> {
                throw new DuplicatePaymentException(
                    "Payment with idempotency key already exists: " + request.getIdempotencyKey()
                );
            });

        // Create payment entity
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setPayerUserId(request.getPayerUserId());
        payment.setPayeeUserId(request.getPayeeUserId());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        payment.setPaymentType(request.getPaymentType() != null ? 
            PaymentType.valueOf(request.getPaymentType()) : PaymentType.PAYMENT);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setDescription(request.getDescription());
        payment.setReference(request.getReference());

        payment = paymentRepository.save(payment);
        logger.info("Initiated payment: {} for payer: {}", payment.getPaymentId(), payment.getPayerUserId());

        // Publish payment.initiated event
        publishPaymentInitiatedEvent(payment, request.getPayerUserId());

        return mapToResponse(payment);
    }

    /**
     * Get payment by paymentId
     */
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        
        return mapToResponse(payment);
    }

    /**
     * Process payment when wallet balance is updated
     * This is called by the Kafka consumer when wallet.balance.updated event is received
     */
    @Transactional
    public void processPaymentOnBalanceUpdate(WalletBalanceUpdatedEvent event) {
        // Find pending payments for this user
        List<Payment> pendingPayments = paymentRepository.findPendingPaymentsByUserId(
            event.getUserId(), 
            PaymentStatus.INITIATED
        );

        for (Payment payment : pendingPayments) {
            try {
                // Check if balance is sufficient
                if (event.getNewBalance().compareTo(payment.getAmount()) >= 0) {
                    // Authorize payment
                    authorizePayment(payment);
                    
                    // Complete payment
                    completePayment(payment, event.getTransactionId());
                    
                    logger.info("Payment {} processed successfully after balance update", payment.getPaymentId());
                } else {
                    // Insufficient balance
                    failPayment(payment, "INSUFFICIENT_FUNDS", "INSUFFICIENT_FUNDS", 
                              "Insufficient balance for payment");
                    logger.warn("Payment {} failed due to insufficient balance", payment.getPaymentId());
                }
            } catch (Exception e) {
                logger.error("Error processing payment {} after balance update", payment.getPaymentId(), e);
                failPayment(payment, "PROCESSING_ERROR", "PROCESSING_ERROR", 
                          "Error processing payment: " + e.getMessage());
            }
        }
    }

    /**
     * Authorize payment
     */
    @Transactional
    public void authorizePayment(Payment payment) {
        if (payment.getStatus() != PaymentStatus.INITIATED) {
            logger.warn("Payment {} is not in INITIATED status, cannot authorize", payment.getPaymentId());
            return;
        }

        payment.setStatus(PaymentStatus.AUTHORIZED);
        payment.setAuthorizedAt(java.time.LocalDateTime.now());
        payment = paymentRepository.save(payment);

        logger.info("Authorized payment: {}", payment.getPaymentId());

        // Publish payment.authorized event
        publishPaymentAuthorizedEvent(payment);
    }

    /**
     * Complete payment
     */
    @Transactional
    public void completePayment(Payment payment, String transactionId) {
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            logger.warn("Payment {} is not in AUTHORIZED status, cannot complete", payment.getPaymentId());
            return;
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(java.time.LocalDateTime.now());
        if (transactionId != null && !transactionId.isEmpty()) {
            try {
                payment.setTransactionId(UUID.fromString(transactionId));
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid transaction ID format: {}, generating new UUID", transactionId);
                payment.setTransactionId(UUID.randomUUID());
            }
        } else {
            payment.setTransactionId(UUID.randomUUID());
        }
        payment = paymentRepository.save(payment);

        logger.info("Completed payment: {} with transaction: {}", payment.getPaymentId(), transactionId);

        // Publish payment.completed event
        publishPaymentCompletedEvent(payment);
    }

    /**
     * Fail payment
     */
    @Transactional
    public void failPayment(Payment payment, String failureReason, String errorCode, String errorMessage) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(failureReason);
        payment.setErrorCode(errorCode);
        payment.setErrorMessage(errorMessage);
        payment.setFailedAt(java.time.LocalDateTime.now());
        payment = paymentRepository.save(payment);

        logger.info("Failed payment: {} with reason: {}", payment.getPaymentId(), failureReason);

        // Publish payment.failed event
        publishPaymentFailedEvent(payment);
    }

    /**
     * Publish payment.initiated event
     */
    private void publishPaymentInitiatedEvent(Payment payment, UUID initiatedBy) {
        try {
            PaymentInitiatedEvent event = new PaymentInitiatedEvent(
                payment.getPaymentId(),
                payment.getIdempotencyKey(),
                payment.getPayerUserId(),
                payment.getPayeeUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getPaymentType().name(),
                payment.getDescription(),
                payment.getReference(),
                initiatedBy
            );

            kafkaTemplate.send(PAYMENT_INITIATED_TOPIC, payment.getPaymentId().toString(), event);
            logger.debug("Published payment.initiated event for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to publish payment.initiated event for payment: {}", payment.getPaymentId(), e);
        }
    }

    /**
     * Publish payment.authorized event
     */
    private void publishPaymentAuthorizedEvent(Payment payment) {
        try {
            PaymentAuthorizedEvent event = new PaymentAuthorizedEvent(
                payment.getPaymentId(),
                payment.getPayerUserId(),
                payment.getPayeeUserId(),
                payment.getAmount(),
                payment.getCurrency()
            );

            kafkaTemplate.send(PAYMENT_AUTHORIZED_TOPIC, payment.getPaymentId().toString(), event);
            logger.debug("Published payment.authorized event for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to publish payment.authorized event for payment: {}", payment.getPaymentId(), e);
        }
    }

    /**
     * Publish payment.completed event
     */
    private void publishPaymentCompletedEvent(Payment payment) {
        try {
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                payment.getPaymentId(),
                payment.getPayerUserId(),
                payment.getPayeeUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTransactionId()
            );

            kafkaTemplate.send(PAYMENT_COMPLETED_TOPIC, payment.getPaymentId().toString(), event);
            logger.debug("Published payment.completed event for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to publish payment.completed event for payment: {}", payment.getPaymentId(), e);
        }
    }

    /**
     * Publish payment.failed event
     */
    private void publishPaymentFailedEvent(Payment payment) {
        try {
            PaymentFailedEvent event = new PaymentFailedEvent(
                payment.getPaymentId(),
                payment.getPayerUserId(),
                payment.getPayeeUserId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getFailureReason(),
                payment.getErrorCode(),
                payment.getErrorMessage()
            );

            kafkaTemplate.send(PAYMENT_FAILED_TOPIC, payment.getPaymentId().toString(), event);
            logger.debug("Published payment.failed event for payment: {}", payment.getPaymentId());
        } catch (Exception e) {
            logger.error("Failed to publish payment.failed event for payment: {}", payment.getPaymentId(), e);
        }
    }

    /**
     * Map Payment entity to PaymentResponse DTO
     */
    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setPaymentId(payment.getPaymentId());
        response.setPayerUserId(payment.getPayerUserId());
        response.setPayeeUserId(payment.getPayeeUserId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setPaymentType(payment.getPaymentType().name());
        response.setStatus(payment.getStatus().name());
        response.setFailureReason(payment.getFailureReason());
        response.setErrorCode(payment.getErrorCode());
        response.setErrorMessage(payment.getErrorMessage());
        response.setDescription(payment.getDescription());
        response.setReference(payment.getReference());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setAuthorizedAt(payment.getAuthorizedAt());
        response.setCompletedAt(payment.getCompletedAt());
        response.setFailedAt(payment.getFailedAt());
        return response;
    }
}

