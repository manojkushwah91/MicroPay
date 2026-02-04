package com.micropay.payment.exception;

/**
 * Exception thrown when a duplicate payment is detected (idempotency violation)
 */
public class DuplicatePaymentException extends RuntimeException {

    public DuplicatePaymentException(String message) {
        super(message);
    }

    public DuplicatePaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}







