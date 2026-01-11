package com.micropay.transaction.service;

import com.micropay.transaction.dto.PaymentCompletedEvent;
import com.micropay.transaction.dto.TransactionRecordedEvent;
import com.micropay.transaction.dto.TransactionResponse;
import com.micropay.transaction.exception.TransactionNotFoundException;
import com.micropay.transaction.exception.TransactionProcessingException;
import com.micropay.transaction.model.Transaction;
import com.micropay.transaction.model.TransactionEntry;
import com.micropay.transaction.model.TransactionEntryType;
import com.micropay.transaction.model.TransactionStatus;
import com.micropay.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service layer for transaction operations
 */
@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    
    private static final String TRANSACTION_RECORDED_TOPIC = "transaction.recorded";

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransactionRecordedEvent> kafkaTemplate;

    public TransactionService(TransactionRepository transactionRepository, 
                            KafkaTemplate<String, TransactionRecordedEvent> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Record transaction from payment.completed event
     */
    @Transactional
    public void recordTransactionFromPayment(PaymentCompletedEvent event) {
        try {
            // Check if transaction already exists for this payment
            if (transactionRepository.findByPaymentId(event.getPaymentId()).isPresent()) {
                logger.warn("Transaction already exists for payment: {}, skipping", event.getPaymentId());
                return;
            }

            // Create transaction
            Transaction transaction = new Transaction(event.getPaymentId());
            
            // Use transactionId from payment event if available, otherwise generate new one
            if (event.getTransactionId() != null) {
                transaction.setTransactionId(event.getTransactionId());
            }

            // Create transaction entries (double-entry bookkeeping)
            // Debit entry for payer
            TransactionEntry debitEntry = new TransactionEntry(
                event.getPayerUserId(),
                TransactionEntryType.DEBIT,
                event.getAmount(),
                event.getCurrency() != null ? event.getCurrency() : "USD"
            );
            transaction.addEntry(debitEntry);

            // Credit entry for payee (if payee exists)
            if (event.getPayeeUserId() != null) {
                TransactionEntry creditEntry = new TransactionEntry(
                    event.getPayeeUserId(),
                    TransactionEntryType.CREDIT,
                    event.getAmount(),
                    event.getCurrency() != null ? event.getCurrency() : "USD"
                );
                transaction.addEntry(creditEntry);
            }

            // Save transaction
            transaction.setStatus(TransactionStatus.RECORDED);
            transaction.setRecordedAt(java.time.LocalDateTime.now());
            transaction = transactionRepository.save(transaction);

            logger.info("Recorded transaction: {} for payment: {}", transaction.getTransactionId(), event.getPaymentId());

            // Publish transaction.recorded event
            publishTransactionRecordedEvent(transaction);

        } catch (Exception e) {
            logger.error("Failed to record transaction for payment: {}", event.getPaymentId(), e);
            throw new TransactionProcessingException(
                "Failed to record transaction for payment: " + event.getPaymentId(), e
            );
        }
    }

    /**
     * Get transaction by transactionId
     */
    public TransactionResponse getTransaction(UUID transactionId) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + transactionId));
        
        return mapToResponse(transaction);
    }

    /**
     * Get all transactions for a user
     */
    public List<TransactionResponse> getTransactionsByUserId(UUID userId) {
        List<Transaction> transactions = transactionRepository.findByUserId(userId);
        return transactions.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Publish transaction.recorded event to Kafka
     */
    private void publishTransactionRecordedEvent(Transaction transaction) {
        try {
            List<TransactionRecordedEvent.TransactionEntryDto> entryDtos = transaction.getEntries().stream()
                .map(entry -> new TransactionRecordedEvent.TransactionEntryDto(
                    entry.getUserId(),
                    entry.getEntryType().name(),
                    entry.getAmount(),
                    entry.getCurrency()
                ))
                .collect(Collectors.toList());

            TransactionRecordedEvent event = new TransactionRecordedEvent(
                transaction.getTransactionId(),
                transaction.getPaymentId(),
                entryDtos
            );

            kafkaTemplate.send(TRANSACTION_RECORDED_TOPIC, transaction.getTransactionId().toString(), event);
            logger.debug("Published transaction.recorded event for transaction: {}", transaction.getTransactionId());
        } catch (Exception e) {
            logger.error("Failed to publish transaction.recorded event for transaction: {}", 
                        transaction.getTransactionId(), e);
            // Note: In production, consider implementing retry mechanism or dead letter queue
        }
    }

    /**
     * Map Transaction entity to TransactionResponse DTO
     */
    private TransactionResponse mapToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionId(transaction.getTransactionId());
        response.setPaymentId(transaction.getPaymentId());
        response.setStatus(transaction.getStatus().name());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        response.setRecordedAt(transaction.getRecordedAt());
        response.setFailedAt(transaction.getFailedAt());
        response.setFailureReason(transaction.getFailureReason());

        // Map entries
        List<TransactionResponse.TransactionEntryResponse> entryResponses = transaction.getEntries().stream()
            .map(entry -> {
                TransactionResponse.TransactionEntryResponse entryResponse = 
                    new TransactionResponse.TransactionEntryResponse();
                entryResponse.setId(entry.getId());
                entryResponse.setUserId(entry.getUserId());
                entryResponse.setEntryType(entry.getEntryType().name());
                entryResponse.setAmount(entry.getAmount());
                entryResponse.setCurrency(entry.getCurrency());
                return entryResponse;
            })
            .collect(Collectors.toList());
        response.setEntries(entryResponses);

        return response;
    }
}

