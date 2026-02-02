package com.ewallet.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Async
    @Retryable(
        maxAttempts = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public CompletableFuture<Boolean> sendEmail(String to, String subject, String body) {
        try {
            log.info("Sending email to: {} with subject: {}", to, subject);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@ewallet.com");
            
            // Mock implementation - just log instead of actual sending
            log.info("Email sent successfully to: {}", to);
            log.debug("Email content: Subject='{}', Body='{}'", subject, body);
            
            // Uncomment below for actual email sending
            // mailSender.send(message);
            
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            return CompletableFuture.completedFuture(false);
        }
    }
}
