package com.ewallet.payment.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Getter
@Setter
public class PaymentRequest {

    Long customerId;
    BigDecimal amount;
    Long merchantAccountNo;
    String productCurrency;

}
