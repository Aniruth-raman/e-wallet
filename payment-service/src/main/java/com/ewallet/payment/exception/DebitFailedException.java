package com.ewallet.payment.exception;

public class DebitFailedException extends RuntimeException {
    public DebitFailedException(String message, Exception ex) {
        super(message, ex);
    }
}
