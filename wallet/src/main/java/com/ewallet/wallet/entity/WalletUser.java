package com.ewallet.wallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wallet_user")
public class WalletUser {

	@Id
	@Column(name = "user_id", nullable = false, unique = true)
	private String userId;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "wallet_acc_no", nullable = false, unique = true)
	private String walletAccNo;

	@Column(name = "currency", nullable = false)
	private String currency;

}
