package com.ewallet.merchant.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String transactionId) {
        super("Transaction not found with id: " + transactionId);
    }
}
