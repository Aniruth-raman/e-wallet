package com.ewallet.payment.client;

import com.ewallet.payment.dto.MerchantCreditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "merchant-service", fallback = MerchantServiceClientFallback.class)
public interface MerchantServiceClient {
    
    @PostMapping("/api/merchants/credit")
    void creditMerchant(@RequestBody MerchantCreditRequest request);
    
    @PostMapping("/api/merchants/debit")
    void debitMerchant(@RequestBody MerchantCreditRequest request);
}
