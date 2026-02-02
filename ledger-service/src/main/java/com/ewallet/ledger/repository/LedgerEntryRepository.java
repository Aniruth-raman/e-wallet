package com.ewallet.ledger.repository;

import com.ewallet.ledger.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    Optional<LedgerEntry> findByTransactionId(String transactionId);

    Page<LedgerEntry> findByCustomerId(String customerId, Pageable pageable);

    Page<LedgerEntry> findByMerchantId(String merchantId, Pageable pageable);

    boolean existsByTransactionId(String transactionId);
}
