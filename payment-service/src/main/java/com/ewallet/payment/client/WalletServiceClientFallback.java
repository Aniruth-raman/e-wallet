package com.ewallet.payment.client;

import com.ewallet.payment.dto.WalletReservationRequest;
import com.ewallet.payment.dto.WalletReservationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WalletServiceClientFallback implements WalletServiceClient {
    
    @Override
    public BigDecimal validateBalance(String customerId) {
        log.error("Wallet service is unavailable - validateBalance fallback for customer: {}", customerId);
        throw new RuntimeException("Wallet service unavailable");
    }
    
    @Override
    public WalletReservationResponse reserve(WalletReservationRequest request) {
        log.error("Wallet service is unavailable - reserve fallback for transaction: {}", request.getTransactionId());
        throw new RuntimeException("Wallet service unavailable");
    }
    
    @Override
    public void confirmReservation(String transactionId) {
        log.error("Wallet service is unavailable - confirmReservation fallback for transaction: {}", transactionId);
        throw new RuntimeException("Wallet service unavailable");
    }
    
    @Override
    public void releaseReservation(String transactionId) {
        log.error("Wallet service is unavailable - releaseReservation fallback for transaction: {}", transactionId);
        throw new RuntimeException("Wallet service unavailable");
    }
    
    @Override
    public void collectFee(WalletReservationRequest request) {
        log.error("Wallet service is unavailable - collectFee fallback for transaction: {}", request.getTransactionId());
        throw new RuntimeException("Wallet service unavailable");
    }
}
