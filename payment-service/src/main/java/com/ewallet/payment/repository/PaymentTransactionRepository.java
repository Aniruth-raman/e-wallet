package com.ewallet.payment.repository;

import com.ewallet.payment.entity.PaymentTransaction;
import com.ewallet.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {
    Optional<PaymentTransaction> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);
}
