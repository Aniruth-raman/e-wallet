package com.ewallet.wallet.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ewallet.wallet.entity.Wallet;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, String> {

	/**
	 * Calculate balance for a given wallet account. tranType = 'C' means Credit,
	 * 'D' means Debit.
	 */
	@Query("SELECT COALESCE(SUM(CASE WHEN wt.tranType = 'C' THEN wt.amount "
			+ "WHEN wt.tranType = 'D' THEN -wt.amount END), 0) "
			+ "FROM WalletTransaction wt WHERE wt.walletAccNo = :walletAccNo")
	BigDecimal checkBalance(String walletAccNo);

	/**
	 * View all transactions for a given wallet account, ordered by createdTime
	 * ascending.
	 */
	List<Wallet> findByWalletAccNoOrderByCreatedTimeAsc(String walletAccNo);
}
