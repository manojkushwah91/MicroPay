package com.micropay.wallet.service;

import com.micropay.wallet.dto.WalletBalanceUpdatedEvent;
import com.micropay.wallet.dto.WalletResponse;
import com.micropay.wallet.exception.InsufficientBalanceException;
import com.micropay.wallet.exception.WalletNotFoundException;
import com.micropay.wallet.model.Wallet;
import com.micropay.wallet.model.WalletStatus;
import com.micropay.wallet.repository.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service layer for wallet operations
 */
@Service
public class WalletService {

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    private static final String WALLET_BALANCE_UPDATED_TOPIC = "wallet.balance.updated";

    private final WalletRepository walletRepository;
    private final KafkaTemplate<String, WalletBalanceUpdatedEvent> kafkaTemplate;

    public WalletService(WalletRepository walletRepository, 
                        KafkaTemplate<String, WalletBalanceUpdatedEvent> kafkaTemplate) {
        this.walletRepository = walletRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Get wallet by user ID
     */
    public WalletResponse getWalletByUserId(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
        
        return mapToResponse(wallet);
    }

    /**
     * Create wallet for a new user
     */
    @Transactional
    public Wallet createWallet(UUID userId, String currency) {
        if (walletRepository.existsByUserId(userId)) {
            logger.warn("Wallet already exists for user: {}", userId);
            return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));
        }

        Wallet wallet = new Wallet(userId, BigDecimal.ZERO, currency != null ? currency : "USD");
        wallet = walletRepository.save(wallet);
        logger.info("Created wallet for user: {} with ID: {}", userId, wallet.getId());
        return wallet;
    }

    /**
     * Credit wallet balance
     */
    @Transactional
    public WalletResponse creditWallet(UUID userId, BigDecimal amount, String transactionId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active. Current status: " + wallet.getStatus());
        }

        BigDecimal previousBalance = wallet.getBalance();
        BigDecimal newBalance = previousBalance.add(amount);
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        logger.info("Credited wallet for user: {}. Previous balance: {}, Amount: {}, New balance: {}", 
                   userId, previousBalance, amount, newBalance);

        // Publish Kafka event
        publishBalanceUpdatedEvent(wallet, previousBalance, amount, "CREDIT", transactionId);

        return mapToResponse(wallet);
    }

    /**
     * Debit wallet balance
     */
    @Transactional
    public WalletResponse debitWallet(UUID userId, BigDecimal amount, String transactionId) {
        Wallet wallet = walletRepository.findByUserIdWithLock(userId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user: " + userId));

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new IllegalStateException("Wallet is not active. Current status: " + wallet.getStatus());
        }

        BigDecimal currentBalance = wallet.getBalance();
        if (currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                String.format("Insufficient balance. Current balance: %s, Requested amount: %s", 
                             currentBalance, amount)
            );
        }

        BigDecimal previousBalance = currentBalance;
        BigDecimal newBalance = previousBalance.subtract(amount);
        wallet.setBalance(newBalance);
        wallet = walletRepository.save(wallet);

        logger.info("Debited wallet for user: {}. Previous balance: {}, Amount: {}, New balance: {}", 
                   userId, previousBalance, amount, newBalance);

        // Publish Kafka event
        publishBalanceUpdatedEvent(wallet, previousBalance, amount.negate(), "DEBIT", transactionId);

        return mapToResponse(wallet);
    }

    /**
     * Publish wallet balance updated event to Kafka
     */
    private void publishBalanceUpdatedEvent(Wallet wallet, BigDecimal previousBalance, 
                                          BigDecimal changeAmount, String transactionType, 
                                          String transactionId) {
        try {
            WalletBalanceUpdatedEvent event = new WalletBalanceUpdatedEvent(
                wallet.getId(),
                wallet.getUserId(),
                previousBalance,
                wallet.getBalance(),
                changeAmount,
                wallet.getCurrency(),
                transactionType,
                transactionId != null ? transactionId : UUID.randomUUID().toString()
            );

            kafkaTemplate.send(WALLET_BALANCE_UPDATED_TOPIC, wallet.getUserId().toString(), event);
            logger.debug("Published wallet.balance.updated event for wallet: {}", wallet.getId());
        } catch (Exception e) {
            logger.error("Failed to publish wallet.balance.updated event for wallet: {}", wallet.getId(), e);
            // Note: In production, consider implementing retry mechanism or dead letter queue
        }
    }

    /**
     * Map Wallet entity to WalletResponse DTO
     */
    private WalletResponse mapToResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus().name());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }
}




