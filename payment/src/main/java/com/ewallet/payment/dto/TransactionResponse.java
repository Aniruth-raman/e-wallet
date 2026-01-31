package com.ewallet.payment.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    String status;
    String transactionId;
}
