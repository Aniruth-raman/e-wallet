package com.ewallet.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletReservationRequest {
    private String customerId;
    private BigDecimal amount;
    private String transactionId;
}
