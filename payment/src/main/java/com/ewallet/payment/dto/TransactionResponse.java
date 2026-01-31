package com.ewallet.payment.dto;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class TransactionResponse {
    String status;
    String transactionId;
}
