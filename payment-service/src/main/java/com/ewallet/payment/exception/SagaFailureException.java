package com.ewallet.payment.exception;

public class SagaFailureException extends RuntimeException {
    public SagaFailureException(String message) {
        super(message);
    }
    
    public SagaFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
