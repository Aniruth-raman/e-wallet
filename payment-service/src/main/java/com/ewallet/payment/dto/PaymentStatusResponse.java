package com.ewallet.payment.dto;

import com.ewallet.payment.enums.Currency;
import com.ewallet.payment.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String transactionId;
    private PaymentStatus status;
    private String currentStep;
    private BigDecimal amount;
    private Currency currency;
    private LocalDateTime createdAt;
}
