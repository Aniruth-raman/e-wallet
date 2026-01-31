package com.ewallet.notification.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ewallet.notification.dto.NotificationRequest;
import com.ewallet.notification.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
	
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest request) {
    	logger.info("inside sendNotification method");
        notificationService.processNotification(request);
        return ResponseEntity.ok("Notification sent successfully.");
    }
}	