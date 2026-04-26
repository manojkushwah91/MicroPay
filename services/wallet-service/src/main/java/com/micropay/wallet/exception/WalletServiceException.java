package com.micropay.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class WalletServiceException extends RuntimeException {
    
    public WalletServiceException(String message) {
        super(message);
    }
    
    public WalletServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
