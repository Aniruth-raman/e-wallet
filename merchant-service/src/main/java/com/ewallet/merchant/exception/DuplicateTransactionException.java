package com.ewallet.merchant.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String transactionId) {
        super("Transaction already exists with id: " + transactionId);
    }
}
