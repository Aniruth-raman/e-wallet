package com.ewallet.merchant.repository;

import com.ewallet.merchant.entity.MerchantTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MerchantTransactionRepository extends JpaRepository<MerchantTransaction, UUID> {
    Optional<MerchantTransaction> findByTransactionId(String transactionId);
}
