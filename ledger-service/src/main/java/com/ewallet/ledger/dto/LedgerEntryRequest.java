package com.ewallet.ledger.dto;

import com.ewallet.ledger.entity.LedgerEntry;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntryRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    private String merchantId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @NotNull(message = "Transaction type is required")
    private LedgerEntry.TransactionType transactionType;

    private BigDecimal customerBalanceBefore;

    private BigDecimal customerBalanceAfter;

    private BigDecimal merchantBalanceBefore;

    private BigDecimal merchantBalanceAfter;

    private String productName;

    @Size(max = 1000, message = "Product description must not exceed 1000 characters")
    private String productDescription;
}
