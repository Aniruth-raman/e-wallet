package com.ewallet.notification.entity;

import java.time.LocalDateTime;

import com.ewallet.notification.dto.NotificationRequest;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "notifications")
@Data 
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Double amount;
    private String transactionStatus;
    private String message;

    public NotificationEntity(NotificationRequest request) {
        this.userId = request.getUserId();
        this.amount = request.getAmount();
        this.transactionStatus = request.getTransactionStatus();
        this.message = request.getMessage();
    }
}