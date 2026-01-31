package com.ewallet.wallet.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserBalanceDTO {

	private String userId;
	private String walletAccNo;
	private BigDecimal balance;
	private String currency;

}
