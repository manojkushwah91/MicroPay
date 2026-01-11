package com.micropay.transaction.controller;

import com.micropay.transaction.dto.TransactionResponse;
import com.micropay.transaction.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for transaction operations
 */
@RestController
@RequestMapping
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * GET /transactions/{userId}
     * Fetch all transactions for a user
     */
    @GetMapping("/transactions/{userId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByUserId(@PathVariable UUID userId) {
        logger.info("Fetching transactions for user: {}", userId);
        List<TransactionResponse> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /transaction/{transactionId}
     * Fetch transaction details
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable UUID transactionId) {
        logger.info("Fetching transaction: {}", transactionId);
        TransactionResponse transaction = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(transaction);
    }
}




