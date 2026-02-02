package com.ewallet.notification.repository;

import com.ewallet.notification.entity.Notification;
import com.ewallet.notification.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    List<Notification> findByStatusOrderByCreatedAtDesc(NotificationStatus status);
    
    List<Notification> findByTransactionId(String transactionId);
}
