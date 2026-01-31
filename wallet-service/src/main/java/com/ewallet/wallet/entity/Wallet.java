package com.ewallet.wallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "wallet_acc_no", nullable = false, unique = true)
    private String walletAccNo;

    @Column(name = "tran_type", nullable = false)
    private String tranType;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    public Wallet(String walletAccNo, String tranType, BigDecimal amount, LocalDateTime createdTime) {
        this.walletAccNo = walletAccNo;
        this.tranType = tranType;
        this.amount = amount;
        this.createdTime = createdTime;
    }
}
