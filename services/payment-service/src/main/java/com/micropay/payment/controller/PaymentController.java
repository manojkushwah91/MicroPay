package com.micropay.payment.controller;

import com.micropay.payment.dto.PaymentRequest;
import com.micropay.payment.dto.PaymentResponse;
import com.micropay.payment.dto.RefundRequest;
import com.micropay.payment.dto.VerifyFundsRequest;
import com.micropay.payment.dto.VerifyFundsResponse;
import com.micropay.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * REST controller for payment operations
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * POST /payment/verify-funds
     * Verify if user has sufficient funds
     */
    @PostMapping("/verify-funds")
    public ResponseEntity<VerifyFundsResponse> verifyFunds(@Valid @RequestBody VerifyFundsRequest request) {
        logger.info("Verifying funds for user: {} with amount: {}", request.getUserId(), request.getAmount());
        VerifyFundsResponse response = paymentService.verifyFunds(request.getUserId(), request.getAmount(), request.getCurrency());
        return ResponseEntity.ok(response);
    }

    /**
     * POST /payment/{paymentId}/refund
     * Refund a payment
     */
    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable UUID paymentId, @Valid @RequestBody RefundRequest request) {
        logger.info("Refunding payment: {} with amount: {}", paymentId, request.getAmount());
        PaymentResponse payment = paymentService.refundPayment(paymentId, request.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * POST /payment
     * Initiate a payment
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        logger.info("Initiating payment for payer: {} with amount: {}", request.getPayerUserId(), request.getAmount());
        PaymentResponse payment = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    /**
     * GET /payment/{paymentId}
     * Fetch payment status
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable UUID paymentId) {
        logger.info("Fetching payment: {}", paymentId);
        PaymentResponse payment = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(payment);
    }
}







