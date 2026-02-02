package com.ewallet.payment.client;

import com.ewallet.payment.dto.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationServiceClientFallback implements NotificationServiceClient {
    
    @Override
    public void notifyCustomer(NotificationRequest request) {
        log.warn("Notification service is unavailable - notifyCustomer fallback for: {}", request.getRecipient());
    }
    
    @Override
    public void notifyMerchant(NotificationRequest request) {
        log.warn("Notification service is unavailable - notifyMerchant fallback for: {}", request.getRecipient());
    }
}
