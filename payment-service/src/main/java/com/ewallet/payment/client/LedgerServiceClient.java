package com.ewallet.payment.client;

import com.ewallet.payment.dto.LedgerEntryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ledger-service", fallback = LedgerServiceClientFallback.class)
public interface LedgerServiceClient {
    
    @PostMapping("/api/ledger/entry")
    void createLedgerEntry(@RequestBody LedgerEntryRequest request);
    
    @PostMapping("/api/ledger/reverse/{transactionId}")
    void reverseLedgerEntry(@PathVariable("transactionId") String transactionId);
}
