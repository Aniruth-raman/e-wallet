package com.ewallet.merchant.controller;

import com.ewallet.merchant.dto.CreditRequest;
import com.ewallet.merchant.dto.DebitRequest;
import com.ewallet.merchant.dto.MerchantResponse;
import com.ewallet.merchant.dto.TransactionResponse;
import com.ewallet.merchant.entity.Currency;
import com.ewallet.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/merchants")
@Tag(name = "Merchant API", description = "APIs for merchant wallet management")
public class MerchantController {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantController.class);
    
    private final MerchantService merchantService;
    
    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }
    
    @GetMapping("/{merchantId}/balance")
    @Operation(summary = "Get merchant balance", description = "Retrieve merchant wallet balance by merchant ID and currency")
    public ResponseEntity<MerchantResponse> getMerchantBalance(
            @PathVariable UUID merchantId,
            @RequestParam(defaultValue = "USD") Currency currency) {
        logger.info("Received request to get balance for merchant: {} with currency: {}", merchantId, currency);
        MerchantResponse response = merchantService.getMerchantBalance(merchantId, currency);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{merchantId}/credit")
    @Operation(summary = "Credit merchant account", description = "Add funds to merchant wallet")
    public ResponseEntity<TransactionResponse> creditMerchant(
            @PathVariable UUID merchantId,
            @Valid @RequestBody CreditRequest request) {
        logger.info("Received credit request for merchant: {}", merchantId);
        request.setMerchantId(merchantId);
        TransactionResponse response = merchantService.creditMerchant(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{merchantId}/debit")
    @Operation(summary = "Debit merchant account", description = "Compensating transaction to reverse credit")
    public ResponseEntity<TransactionResponse> debitMerchant(
            @PathVariable UUID merchantId,
            @Valid @RequestBody DebitRequest request) {
        logger.info("Received debit request for merchant: {}", merchantId);
        request.setMerchantId(merchantId);
        TransactionResponse response = merchantService.debitMerchant(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/transactions/{transactionId}/status")
    @Operation(summary = "Get transaction status", description = "Retrieve transaction status by transaction ID")
    public ResponseEntity<TransactionResponse> getTransactionStatus(@PathVariable String transactionId) {
        logger.info("Received request to get transaction status for transactionId: {}", transactionId);
        TransactionResponse response = merchantService.getTransactionStatus(transactionId);
        return ResponseEntity.ok(response);
    }
}
