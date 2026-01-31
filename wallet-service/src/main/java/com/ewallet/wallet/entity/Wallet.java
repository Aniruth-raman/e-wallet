package com.ewallet.wallet.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
<<<<<<< HEAD
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
=======
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
>>>>>>> 97ca1f4 (commit 2)
@Entity
@Table(name = "wallet")
public class Wallet {

	@Id
<<<<<<< HEAD
	@Column(name = "wallet_acc_no", nullable = false, unique = true)
=======
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transaction_id")
	private Long transactionId;

	@Column(name = "wallet_acc_no", nullable = false)
>>>>>>> 97ca1f4 (commit 2)
	private String walletAccNo;

	@Column(name = "tran_type", nullable = false)
	private String tranType;

	@Column(name = "amount", nullable = false, precision = 15, scale = 2)
	private BigDecimal amount;

	@Column(name = "created_time", nullable = false)
	private LocalDateTime createdTime;

<<<<<<< HEAD
=======
	public Wallet(String walletAccNo, String tranType, BigDecimal amount, LocalDateTime createdTime) {
		this.walletAccNo = walletAccNo;
		this.tranType = tranType;
		this.amount = amount;
		this.createdTime = createdTime;
	}

>>>>>>> 97ca1f4 (commit 2)
}
