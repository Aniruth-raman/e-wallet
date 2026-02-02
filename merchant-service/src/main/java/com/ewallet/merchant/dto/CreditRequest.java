package com.ewallet.merchant.dto;

import com.ewallet.merchant.entity.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditRequest {
    
    @NotNull(message = "Merchant ID is required")
    private UUID merchantId;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    @NotNull(message = "Transaction ID is required")
    private String transactionId;
}
