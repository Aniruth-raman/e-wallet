package com.ewallet.merchant.dto;

import com.ewallet.merchant.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private TransactionStatus status;
    private BigDecimal amount;
}
