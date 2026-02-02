package com.ewallet.merchant.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(BigDecimal required, BigDecimal available) {
        super("Insufficient funds. Required: " + required + ", Available: " + available);
    }
}
