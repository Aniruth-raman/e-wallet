package com.ewallet.notification.service;

import com.ewallet.notification.dto.NotificationRequest;
import com.ewallet.notification.dto.NotificationResponse;
import com.ewallet.notification.entity.AuditLog;
import com.ewallet.notification.entity.Notification;
import com.ewallet.notification.enums.NotificationStatus;
import com.ewallet.notification.exception.NotificationFailedException;
import com.ewallet.notification.repository.AuditLogRepository;
import com.ewallet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    
    @Transactional
    public NotificationResponse sendCustomerNotification(NotificationRequest request) {
        log.info("Processing customer notification for recipient: {}", request.getRecipientId());
        return processNotification(request, "CUSTOMER_NOTIFICATION");
    }
    
    @Transactional
    public NotificationResponse sendMerchantNotification(NotificationRequest request) {
        log.info("Processing merchant notification for recipient: {}", request.getRecipientId());
        return processNotification(request, "MERCHANT_NOTIFICATION");
    }
    
    private NotificationResponse processNotification(NotificationRequest request, String action) {
        try {
            // Create notification entity
            Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .recipientEmail(request.getRecipientEmail())
                .recipientName(request.getRecipientName())
                .type(request.getType())
                .subject(request.getSubject())
                .message(request.getMessage())
                .transactionId(request.getTransactionId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(NotificationStatus.PENDING)
                .build();
            
            notification = notificationRepository.save(notification);
            log.info("Notification created with ID: {}", notification.getId());
            
            // Log audit - notification created
            createAuditLog(notification.getId(), action, "PENDING", "Notification created");
            
            // Send email asynchronously
            UUID notificationId = notification.getId();
            sendEmailAsync(notification)
                .thenAccept(success -> handleEmailResult(notificationId, success, action));
            
            return NotificationResponse.builder()
                .notificationId(notification.getId())
                .status(NotificationStatus.PENDING)
                .message("Notification processing initiated")
                .build();
                
        } catch (Exception e) {
            log.error("Failed to process notification", e);
            throw new NotificationFailedException("Failed to process notification: " + e.getMessage());
        }
    }
    
    @Async
    public CompletableFuture<Boolean> sendEmailAsync(Notification notification) {
        return emailService.sendEmail(
            notification.getRecipientEmail(),
            notification.getSubject(),
            notification.getMessage()
        );
    }
    
    @Transactional
    public void handleEmailResult(UUID notificationId, boolean success, String action) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new NotificationFailedException("Notification not found: " + notificationId));
        
        if (success) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            createAuditLog(notificationId, action, "SENT", "Email sent successfully");
            log.info("Notification {} sent successfully", notificationId);
        } else {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            createAuditLog(notificationId, action, "FAILED", "Email sending failed");
            log.error("Notification {} failed to send", notificationId);
        }
    }
    
    public List<Notification> getNotificationsByRecipient(String recipientId) {
        log.info("Fetching notifications for recipient: {}", recipientId);
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
    }
    
    public List<Notification> getNotificationsByStatus(NotificationStatus status) {
        log.info("Fetching notifications with status: {}", status);
        return notificationRepository.findByStatusOrderByCreatedAtDesc(status);
    }
    
    private void createAuditLog(UUID notificationId, String action, String status, String details) {
        AuditLog auditLog = AuditLog.builder()
            .notificationId(notificationId)
            .action(action)
            .status(status)
            .details(details)
            .build();
        auditLogRepository.save(auditLog);
        log.debug("Audit log created for notification: {}", notificationId);
    }
}
