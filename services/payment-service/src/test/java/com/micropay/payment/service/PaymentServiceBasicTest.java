package com.micropay.payment.service;

import com.micropay.payment.dto.PaymentRequest;
import com.micropay.payment.dto.PaymentResponse;
import com.micropay.payment.exception.DuplicatePaymentException;
import com.micropay.payment.exception.PaymentNotFoundException;
import com.micropay.payment.model.Payment;
import com.micropay.payment.model.PaymentStatus;
import com.micropay.payment.model.PaymentType;
import com.micropay.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Basic Tests")
class PaymentServiceBasicTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    private UUID paymentId;
    private UUID payerId;
    private UUID payeeId;
    private PaymentRequest paymentRequest;
    private Payment payment;

    @BeforeEach
    void setUp() {
        paymentId = UUID.randomUUID();
        payerId = UUID.randomUUID();
        payeeId = UUID.randomUUID();

        paymentRequest = new PaymentRequest();
        paymentRequest.setIdempotencyKey("test-idempotency-key");
        paymentRequest.setPayerUserId(payerId);
        paymentRequest.setPayeeUserId(payeeId);
        paymentRequest.setAmount(new BigDecimal("100.00"));
        paymentRequest.setCurrency("USD");
        paymentRequest.setPaymentType("PAYMENT");
        paymentRequest.setDescription("Test payment");
        paymentRequest.setReference("REF-123");

        payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setIdempotencyKey(paymentRequest.getIdempotencyKey());
        payment.setPayerUserId(payerId);
        payment.setPayeeUserId(payeeId);
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setPaymentType(PaymentType.PAYMENT);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setDescription(paymentRequest.getDescription());
        payment.setReference(paymentRequest.getReference());
        payment.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should initiate payment successfully")
    void initiatePayment_Success() {
        // Given
        when(paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
                .thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        PaymentResponse response = paymentService.initiatePayment(paymentRequest);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(payerId, response.getPayerUserId());
        assertEquals(payeeId, response.getPayeeUserId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals(PaymentStatus.INITIATED.name(), response.getStatus());

        verify(paymentRepository).findByIdempotencyKey(paymentRequest.getIdempotencyKey());
        verify(paymentRepository).save(any(Payment.class));
        verify(kafkaTemplate).send(eq("payment.initiated"), eq(paymentId.toString()), any());
    }

    @Test
    @DisplayName("Should throw DuplicatePaymentException when idempotency key exists")
    void initiatePayment_DuplicateIdempotencyKey() {
        // Given
        when(paymentRepository.findByIdempotencyKey(paymentRequest.getIdempotencyKey()))
                .thenReturn(Optional.of(payment));

        // When & Then
        assertThrows(DuplicatePaymentException.class, () -> paymentService.initiatePayment(paymentRequest));

        verify(paymentRepository).findByIdempotencyKey(paymentRequest.getIdempotencyKey());
        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("Should get payment by ID successfully")
    void getPayment_Success() {
        // Given
        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.of(payment));

        // When
        PaymentResponse response = paymentService.getPayment(paymentId);

        // Then
        assertNotNull(response);
        assertEquals(paymentId, response.getPaymentId());
        assertEquals(PaymentStatus.INITIATED.name(), response.getStatus());

        verify(paymentRepository).findByPaymentId(paymentId);
    }

    @Test
    @DisplayName("Should throw PaymentNotFoundException when payment not found")
    void getPayment_NotFound() {
        // Given
        when(paymentRepository.findByPaymentId(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(PaymentNotFoundException.class, () -> paymentService.getPayment(paymentId));

        verify(paymentRepository).findByPaymentId(paymentId);
    }

    @Test
    @DisplayName("Should authorize payment successfully")
    void authorizePayment_Success() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.authorizePayment(payment);

        // Then
        assertEquals(PaymentStatus.AUTHORIZED, payment.getStatus());
        assertNotNull(payment.getAuthorizedAt());

        verify(paymentRepository).save(payment);
        verify(kafkaTemplate).send(eq("payment.authorized"), eq(paymentId.toString()), any());
    }

    @Test
    @DisplayName("Should not authorize payment if not in INITIATED status")
    void authorizePayment_NotInitiated() {
        // Given
        payment.setStatus(PaymentStatus.COMPLETED);

        // When
        paymentService.authorizePayment(payment);

        // Then
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNull(payment.getAuthorizedAt());

        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("Should complete payment successfully")
    void completePayment_Success() {
        // Given
        payment.setStatus(PaymentStatus.AUTHORIZED);
        String transactionId = UUID.randomUUID().toString();
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.completePayment(payment, transactionId);

        // Then
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertNotNull(payment.getCompletedAt());
        assertNotNull(payment.getTransactionId());

        verify(paymentRepository).save(payment);
        verify(kafkaTemplate).send(eq("payment.completed"), eq(paymentId.toString()), any());
    }

    @Test
    @DisplayName("Should not complete payment if not in AUTHORIZED status")
    void completePayment_NotAuthorized() {
        // Given
        payment.setStatus(PaymentStatus.INITIATED);

        // When
        paymentService.completePayment(payment, UUID.randomUUID().toString());

        // Then
        assertEquals(PaymentStatus.INITIATED, payment.getStatus());
        assertNull(payment.getCompletedAt());

        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("Should fail payment successfully")
    void failPayment_Success() {
        // Given
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.failPayment(payment, "INSUFFICIENT_FUNDS", "ERR_001", "Insufficient balance");

        // Then
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
        assertEquals("INSUFFICIENT_FUNDS", payment.getFailureReason());
        assertEquals("ERR_001", payment.getErrorCode());
        assertEquals("Insufficient balance", payment.getErrorMessage());
        assertNotNull(payment.getFailedAt());

        verify(paymentRepository).save(payment);
        verify(kafkaTemplate).send(eq("payment.failed"), eq(paymentId.toString()), any());
    }
}
