package com.ewallet.payment.repository;

import com.ewallet.payment.entity.SagaStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, UUID> {
    List<SagaStep> findByPaymentTransactionIdOrderByCreatedAtAsc(UUID paymentTransactionId);
}
