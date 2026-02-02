package com.ewallet.ledger.exception;

public class LedgerEntryNotFoundException extends RuntimeException {
    public LedgerEntryNotFoundException(String message) {
        super(message);
    }
}
