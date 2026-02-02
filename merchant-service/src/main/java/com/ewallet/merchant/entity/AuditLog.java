package com.ewallet.merchant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "merchant_wallet_id", nullable = false)
    private UUID merchantWalletId;
    
    @Column(nullable = false)
    private String action;
    
    @Column(name = "old_balance", precision = 19, scale = 2)
    private BigDecimal oldBalance;
    
    @Column(name = "new_balance", precision = 19, scale = 2)
    private BigDecimal newBalance;
    
    @Column(precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
