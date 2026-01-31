package com.ewallet.wallet.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletRequestDTO {

	private String userId;

	private BigDecimal amount;
	
	
}