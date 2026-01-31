package com.ewallet.notification.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.ewallet.notification.dto.NotificationRequest;
import com.ewallet.notification.entity.NotificationEntity;
import com.ewallet.notification.repository.NotificationRepo;

@Service
public class NotificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

	@Autowired
	private NotificationRepo notificationRepository;
	 
	
    @Async
    public void processNotification(NotificationRequest request) {
     
        logger.info("SYSTEM NOTIFICATION GENERATED:");
        logger.info("To User: {}", request.getUserId());
        logger.info("amount: {}", request.getAmount());
        logger.info("status: {}", request.getTransactionStatus());
        try {
        	notificationRepository.save(new NotificationEntity(request));
            logger.info("notification sent");
        } catch (Exception e) {
            logger.error("Failed to send email: " + e.getMessage());
        }
    }
}