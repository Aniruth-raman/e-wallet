package com.ewallet.payment.client;

import com.ewallet.payment.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", fallback = NotificationServiceClientFallback.class)
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/send")
    void notifyCustomer(@RequestBody NotificationRequest request);
    
    @PostMapping("/api/notifications/send")
    void notifyMerchant(@RequestBody NotificationRequest request);
}
