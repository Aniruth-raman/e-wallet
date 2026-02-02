package com.ewallet.payment.dto;

import com.ewallet.payment.enums.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotBlank(message = "Merchant ID is required")
    private String merchantId;
    
    @Valid
    @NotNull(message = "Product details are required")
    private Product product;
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    private Currency currency;
    
    private String transactionId;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        @NotBlank(message = "Product name is required")
        private String name;
        
        private String description;
    }
}
