package com.ewallet.ledger.dto;

import com.ewallet.ledger.entity.LedgerEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryResponse {

    private UUID id;
    private String transactionId;
    private String customerId;
    private String merchantId;
    private BigDecimal amount;
    private String currency;
    private LedgerEntry.TransactionType transactionType;
    private LedgerEntry.Status status;
    private BigDecimal customerBalanceBefore;
    private BigDecimal customerBalanceAfter;
    private BigDecimal merchantBalanceBefore;
    private BigDecimal merchantBalanceAfter;
    private String productName;
    private String productDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LedgerEntryResponse from(LedgerEntry entry) {
        return LedgerEntryResponse.builder()
                .id(entry.getId())
                .transactionId(entry.getTransactionId())
                .customerId(entry.getCustomerId())
                .merchantId(entry.getMerchantId())
                .amount(entry.getAmount())
                .currency(entry.getCurrency())
                .transactionType(entry.getTransactionType())
                .status(entry.getStatus())
                .customerBalanceBefore(entry.getCustomerBalanceBefore())
                .customerBalanceAfter(entry.getCustomerBalanceAfter())
                .merchantBalanceBefore(entry.getMerchantBalanceBefore())
                .merchantBalanceAfter(entry.getMerchantBalanceAfter())
                .productName(entry.getProductName())
                .productDescription(entry.getProductDescription())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
