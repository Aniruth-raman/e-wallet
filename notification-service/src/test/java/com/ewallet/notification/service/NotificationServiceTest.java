package com.ewallet.notification.service;

import com.ewallet.notification.dto.NotificationRequest;
import com.ewallet.notification.dto.NotificationResponse;
import com.ewallet.notification.entity.AuditLog;
import com.ewallet.notification.entity.Notification;
import com.ewallet.notification.enums.NotificationStatus;
import com.ewallet.notification.enums.NotificationType;
import com.ewallet.notification.repository.AuditLogRepository;
import com.ewallet.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private AuditLogRepository auditLogRepository;
    
    @Mock
    private EmailService emailService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private NotificationRequest testRequest;
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        testRequest = NotificationRequest.builder()
            .recipientId("customer-123")
            .recipientEmail("customer@example.com")
            .recipientName("John Doe")
            .type(NotificationType.CUSTOMER_PAYMENT)
            .subject("Payment Confirmation")
            .message("Your payment of $100 was successful")
            .transactionId("tx-123")
            .amount(new BigDecimal("100.00"))
            .currency("USD")
            .build();
        
        testNotification = Notification.builder()
            .id(UUID.randomUUID())
            .recipientId(testRequest.getRecipientId())
            .recipientEmail(testRequest.getRecipientEmail())
            .recipientName(testRequest.getRecipientName())
            .type(testRequest.getType())
            .subject(testRequest.getSubject())
            .message(testRequest.getMessage())
            .transactionId(testRequest.getTransactionId())
            .amount(testRequest.getAmount())
            .currency(testRequest.getCurrency())
            .status(NotificationStatus.PENDING)
            .build();
    }
    
    @Test
    void testSendCustomerNotification_Success() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(new AuditLog());
        
        // Note: sendEmailAsync is called asynchronously so we don't mock it for this test
        // The test validates notification creation and initial processing only
        
        NotificationResponse response = notificationService.sendCustomerNotification(testRequest);
        
        assertNotNull(response);
        assertNotNull(response.getNotificationId());
        assertEquals(NotificationStatus.PENDING, response.getStatus());
        assertEquals("Notification processing initiated", response.getMessage());
        
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
    
    @Test
    void testGetNotificationsByRecipient() {
        List<Notification> notifications = List.of(testNotification);
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc("customer-123"))
            .thenReturn(notifications);
        
        List<Notification> result = notificationService.getNotificationsByRecipient("customer-123");
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotification.getId(), result.get(0).getId());
        
        verify(notificationRepository, times(1))
            .findByRecipientIdOrderByCreatedAtDesc("customer-123");
    }
}
