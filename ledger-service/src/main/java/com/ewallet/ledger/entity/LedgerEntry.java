package com.ewallet.ledger.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries", indexes = {
    @Index(name = "idx_transaction_id", columnList = "transaction_id"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_merchant_id", columnList = "merchant_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "merchant_id")
    private String merchantId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "customer_balance_before", precision = 19, scale = 2)
    private BigDecimal customerBalanceBefore;

    @Column(name = "customer_balance_after", precision = 19, scale = 2)
    private BigDecimal customerBalanceAfter;

    @Column(name = "merchant_balance_before", precision = 19, scale = 2)
    private BigDecimal merchantBalanceBefore;

    @Column(name = "merchant_balance_after", precision = 19, scale = 2)
    private BigDecimal merchantBalanceAfter;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_description", length = 1000)
    private String productDescription;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TransactionType {
        PAYMENT, FEE, REFUND
    }

    public enum Status {
        PENDING, COMPLETED, REVERSED
    }
}
