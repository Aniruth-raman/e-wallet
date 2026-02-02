package com.ewallet.payment.client;

import com.ewallet.payment.dto.MerchantCreditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MerchantServiceClientFallback implements MerchantServiceClient {
    
    @Override
    public void creditMerchant(MerchantCreditRequest request) {
        log.error("Merchant service is unavailable - creditMerchant fallback for transaction: {}", request.getTransactionId());
        throw new RuntimeException("Merchant service unavailable");
    }
    
    @Override
    public void debitMerchant(MerchantCreditRequest request) {
        log.error("Merchant service is unavailable - debitMerchant fallback for transaction: {}", request.getTransactionId());
        throw new RuntimeException("Merchant service unavailable");
    }
}
