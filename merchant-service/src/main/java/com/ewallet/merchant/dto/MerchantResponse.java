package com.ewallet.merchant.dto;

import com.ewallet.merchant.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantResponse {
    private UUID merchantId;
    private String name;
    private String email;
    private String businessType;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
}
