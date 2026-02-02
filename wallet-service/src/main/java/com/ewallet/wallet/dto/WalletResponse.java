package com.ewallet.wallet.dto;

import com.ewallet.wallet.entity.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletResponse {
    private UUID walletId;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String accountNumber;
    private BigDecimal balance;
    private Currency currency;
}
