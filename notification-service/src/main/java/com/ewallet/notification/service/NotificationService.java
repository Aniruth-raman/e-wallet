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
    
//	@Autowired
//    private JavaMailSender mailSender;

	@Autowired
	private NotificationRepo notificationRepository;
//	@Value("${spring.mail.username}")
//	private String senderEmail;
	 
	
    @Async
    public void processNotification(NotificationRequest request) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(senderEmail);
//        logger.info("mail id from: "+senderEmail);
//        message.setTo(senderEmail);
//        message.setSubject("Status Update: " + request.getTransactionStatus());
//        message.setText("your transaction of " + request.getAmount() + " was " + request.getTransactionStatus());
        
        logger.info("SYSTEM NOTIFICATION GENERATED:");
        logger.info("To User: {}", request.getRecipientEmail());
        logger.info("amount: {}", request.getAmount());
        logger.info("status: {}", request.getTransactionStatus());
        try {
//            mailSender.send(message);
        	notificationRepository.save(new NotificationEntity(request));
            logger.info("notification sent");
        } catch (MailException e) {
            logger.error("Failed to send email: " + e.getMessage());
        }
    }
}