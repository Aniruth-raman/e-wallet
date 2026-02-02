package com.ewallet.merchant.repository;

import com.ewallet.merchant.entity.Currency;
import com.ewallet.merchant.entity.MerchantWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantWalletRepository extends JpaRepository<MerchantWallet, UUID> {
    Optional<MerchantWallet> findByMerchantIdAndCurrency(UUID merchantId, Currency currency);
}
