package com.ewallet.payment.client;

import com.ewallet.payment.dto.WalletReservationRequest;
import com.ewallet.payment.dto.WalletReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@FeignClient(name = "wallet-service", fallback = WalletServiceClientFallback.class)
public interface WalletServiceClient {
    
    @GetMapping("/api/wallets/{customerId}/balance")
    BigDecimal validateBalance(@PathVariable("customerId") String customerId);
    
    @PostMapping("/api/wallets/reserve")
    WalletReservationResponse reserve(@RequestBody WalletReservationRequest request);
    
    @PostMapping("/api/wallets/confirm/{transactionId}")
    void confirmReservation(@PathVariable("transactionId") String transactionId);
    
    @PostMapping("/api/wallets/release/{transactionId}")
    void releaseReservation(@PathVariable("transactionId") String transactionId);
    
    @PostMapping("/api/wallets/collect-fee")
    void collectFee(@RequestBody WalletReservationRequest request);
}
