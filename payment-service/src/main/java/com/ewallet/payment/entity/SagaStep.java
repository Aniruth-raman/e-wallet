package com.ewallet.payment.entity;

import com.ewallet.payment.enums.StepStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "saga_steps", indexes = {
    @Index(name = "idx_payment_transaction", columnList = "payment_transaction_id"),
    @Index(name = "idx_step_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    private PaymentTransaction paymentTransaction;
    
    @Column(nullable = false)
    private String stepName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status;
    
    @Column(nullable = false)
    private Integer attempt;
    
    @Column(length = 2000)
    private String errorMessage;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
