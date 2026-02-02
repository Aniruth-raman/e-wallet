package com.ewallet.payment.enums;

public enum PaymentStatus {
    INITIATED,
    WALLET_RESERVED,
    MERCHANT_CREDITED,
    LEDGER_UPDATED,
    NOTIFICATION_SENT,
    FEE_COLLECTED,
    COMPLETED,
    FAILED
}
