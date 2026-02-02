package com.ewallet.payment.client;

import com.ewallet.payment.dto.LedgerEntryRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LedgerServiceClientFallback implements LedgerServiceClient {
    
    @Override
    public void createLedgerEntry(LedgerEntryRequest request) {
        log.error("Ledger service is unavailable - createLedgerEntry fallback for transaction: {}", request.getTransactionId());
        throw new RuntimeException("Ledger service unavailable");
    }
    
    @Override
    public void reverseLedgerEntry(String transactionId) {
        log.error("Ledger service is unavailable - reverseLedgerEntry fallback for transaction: {}", transactionId);
        throw new RuntimeException("Ledger service unavailable");
    }
}
