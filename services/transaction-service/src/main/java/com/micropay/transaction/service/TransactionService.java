package com.micropay.transaction.service;

import com.micropay.transaction.dto.PaymentCompletedEvent;
import com.micropay.transaction.dto.TransactionRecordedEvent;
import com.micropay.transaction.dto.TransactionResponse;
import com.micropay.transaction.dto.TransferRequest;
import com.micropay.transaction.dto.TransactionInitiatedEvent;
import com.micropay.transaction.exception.TransactionNotFoundException;
import com.micropay.transaction.exception.TransactionProcessingException;
import com.micropay.transaction.model.Transaction;
import com.micropay.transaction.model.TransactionEntry;
import com.micropay.transaction.model.TransactionEntryType;
import com.micropay.transaction.model.TransactionStatus;
import com.micropay.transaction.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";

    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> genericKafkaTemplate;
    private final WebClient webClient;

    public TransactionService(TransactionRepository transactionRepository, 
                            @Qualifier("genericKafkaTemplate") KafkaTemplate<String, Object> genericKafkaTemplate,
                            WebClient webClient) {
        this.transactionRepository = transactionRepository;
        this.genericKafkaTemplate = genericKafkaTemplate;
        this.webClient = webClient;
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

            genericKafkaTemplate.send(TRANSACTION_RECORDED_TOPIC, transaction.getTransactionId().toString(), event);
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
    
    /**
     * Initiate a money transfer
     */
    @Transactional
    public TransactionResponse initiateTransfer(TransferRequest request) {
        logger.info("Initiating transfer from {} to {} for amount: {}", 
                   request.getFromUserId(), request.getToUserId(), request.getAmount());
        
        try {
            // Step 1: Verify funds with payment-service
            VerifyFundsResponse fundsResponse = verifyFunds(request.getFromUserId(), request.getAmount(), request.getCurrency());
            
            if (!fundsResponse.isSufficient()) {
                throw new TransactionProcessingException("Insufficient funds for transfer");
            }
            
            // Step 2: Create transaction record
            Transaction transaction = new Transaction();
            transaction.setTransactionId(UUID.randomUUID());
            transaction.setPaymentId(UUID.randomUUID()); // Generate a payment ID for tracking
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setFailureReason(request.getDescription()); // Store description in failureReason for now
            
            transaction = transactionRepository.save(transaction);
            
            // Step 3: Create transaction entries
            createTransactionEntries(transaction, request);
            
            // Step 4: Publish TransactionInitiatedEvent
            publishTransactionInitiatedEvent(transaction, request);
            
            logger.info("Transfer initiated successfully: {}", transaction.getTransactionId());
            
            return mapToResponse(transaction);
            
        } catch (Exception e) {
            logger.error("Error initiating transfer", e);
            throw new TransactionProcessingException("Failed to initiate transfer: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verify funds with payment-service
     */
    private VerifyFundsResponse verifyFunds(UUID userId, BigDecimal amount, String currency) {
        try {
            Mono<VerifyFundsResponse> responseMono = webClient.post()
                    .uri("/payment/verify-funds")
                    .bodyValue(new VerifyFundsRequest(userId, amount, currency))
                    .retrieve()
                    .bodyToMono(VerifyFundsResponse.class);
            
            return responseMono.block();
        } catch (Exception e) {
            logger.error("Error verifying funds for user: {}", userId, e);
            throw new TransactionProcessingException("Failed to verify funds", e);
        }
    }
    
    /**
     * Create debit and credit entries for the transaction
     */
    private void createTransactionEntries(Transaction transaction, TransferRequest request) {
        // Debit entry for sender
        TransactionEntry debitEntry = new TransactionEntry();
        debitEntry.setTransaction(transaction);
        debitEntry.setUserId(request.getFromUserId());
        debitEntry.setEntryType(TransactionEntryType.DEBIT);
        debitEntry.setAmount(request.getAmount());
        debitEntry.setCurrency(request.getCurrency());
        
        // Credit entry for receiver
        TransactionEntry creditEntry = new TransactionEntry();
        creditEntry.setTransaction(transaction);
        creditEntry.setUserId(request.getToUserId());
        creditEntry.setEntryType(TransactionEntryType.CREDIT);
        creditEntry.setAmount(request.getAmount());
        creditEntry.setCurrency(request.getCurrency());
        
        transaction.getEntries().add(debitEntry);
        transaction.getEntries().add(creditEntry);
    }
    
    /**
     * Publish TransactionInitiatedEvent to Kafka
     */
    private void publishTransactionInitiatedEvent(Transaction transaction, TransferRequest request) {
        try {
            TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                transaction.getTransactionId(),
                request.getFromUserId(),
                request.getToUserId(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                transaction.getStatus().name()
            );
            
            genericKafkaTemplate.send(TRANSACTION_INITIATED_TOPIC, transaction.getTransactionId().toString(), event);
            logger.info("Published transaction.initiated event for transaction: {}", transaction.getTransactionId());
        } catch (Exception e) {
            logger.error("Failed to publish transaction.initiated event for transaction: {}", 
                        transaction.getTransactionId(), e);
        }
    }
    
    /**
     * DTOs for service communication
     */
    public static class VerifyFundsRequest {
        private UUID userId;
        private BigDecimal amount;
        private String currency;
        
        public VerifyFundsRequest() {}
        
        public VerifyFundsRequest(UUID userId, BigDecimal amount, String currency) {
            this.userId = userId;
            this.amount = amount;
            this.currency = currency;
        }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
    
    public static class VerifyFundsResponse {
        private boolean sufficient;
        private BigDecimal availableBalance;
        private BigDecimal requestedAmount;
        private String currency;
        
        public boolean isSufficient() { return sufficient; }
        public void setSufficient(boolean sufficient) { this.sufficient = sufficient; }
        public BigDecimal getAvailableBalance() { return availableBalance; }
        public void setAvailableBalance(BigDecimal availableBalance) { this.availableBalance = availableBalance; }
        public BigDecimal getRequestedAmount() { return requestedAmount; }
        public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }
}

