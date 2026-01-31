package com.ewallet.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wallet")
public class Wallet {

	@Id
	@Column(name = "wallet_acc_no", nullable = false, unique = true)
	private String walletAccNo;

	@Column(name = "tran_type", nullable = false)
	private String tranType;

	@Column(name = "amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "created_time", nullable = false)
	private LocalDateTime createdTime;

}
