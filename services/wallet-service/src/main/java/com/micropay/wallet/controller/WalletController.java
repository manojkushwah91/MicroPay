package com.micropay.wallet.controller;

import com.micropay.wallet.dto.CreditRequest;
import com.micropay.wallet.dto.DebitRequest;
import com.micropay.wallet.dto.WalletResponse;
import com.micropay.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for wallet operations
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private static final Logger logger = LoggerFactory.getLogger(WalletController.class);

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * GET /wallet/{userId}
     * Fetch wallet information for a user
     */
    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID userId) {
        logger.info("Fetching wallet for user: {}", userId);
        WalletResponse wallet = walletService.getWalletByUserId(userId);
        return ResponseEntity.ok(wallet);
    }

    /**
     * POST /wallet/{userId}/credit
     * Credit amount to user's wallet
     */
    @PostMapping("/{userId}/credit")
    public ResponseEntity<WalletResponse> creditWallet(
            @PathVariable UUID userId,
            @Valid @RequestBody CreditRequest request) {
        logger.info("Crediting wallet for user: {} with amount: {}", userId, request.getAmount());
        WalletResponse wallet = walletService.creditWallet(
            userId, 
            request.getAmount(), 
            request.getTransactionId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }

    /**
     * POST /wallet/{userId}/debit
     * Debit amount from user's wallet
     */
    @PostMapping("/{userId}/debit")
    public ResponseEntity<WalletResponse> debitWallet(
            @PathVariable UUID userId,
            @Valid @RequestBody DebitRequest request) {
        logger.info("Debiting wallet for user: {} with amount: {}", userId, request.getAmount());
        WalletResponse wallet = walletService.debitWallet(
            userId, 
            request.getAmount(), 
            request.getTransactionId()
        );
        return ResponseEntity.status(HttpStatus.OK).body(wallet);
    }
}







