package com.micropay.transaction.exception;

/**
 * Exception thrown when transaction processing fails
 */
public class TransactionProcessingException extends RuntimeException {

    public TransactionProcessingException(String message) {
        super(message);
    }

    public TransactionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}







