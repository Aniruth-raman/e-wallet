package com.ewallet.payment.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
@Component
@Getter
@Setter
public class BalanceResponse {
    BigDecimal balance;
    String currency;
    String walletAccountNo;
    String userId;
}
