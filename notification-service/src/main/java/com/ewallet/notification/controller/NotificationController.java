package com.ewallet.notification.controller;

import com.ewallet.notification.dto.NotificationRequest;
import com.ewallet.notification.dto.NotificationResponse;
import com.ewallet.notification.entity.Notification;
import com.ewallet.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @PostMapping("/customer")
    @Operation(summary = "Send customer notification")
    public ResponseEntity<NotificationResponse> sendCustomerNotification(
        @Valid @RequestBody NotificationRequest request
    ) {
        log.info("Received customer notification request for: {}", request.getRecipientId());
        NotificationResponse response = notificationService.sendCustomerNotification(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    @PostMapping("/merchant")
    @Operation(summary = "Send merchant notification")
    public ResponseEntity<NotificationResponse> sendMerchantNotification(
        @Valid @RequestBody NotificationRequest request
    ) {
        log.info("Received merchant notification request for: {}", request.getRecipientId());
        NotificationResponse response = notificationService.sendMerchantNotification(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
    
    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get notifications by recipient ID")
    public ResponseEntity<List<Notification>> getNotificationsByRecipient(
        @PathVariable String recipientId
    ) {
        log.info("Fetching notifications for recipient: {}", recipientId);
        List<Notification> notifications = notificationService.getNotificationsByRecipient(recipientId);
        return ResponseEntity.ok(notifications);
    }
}
