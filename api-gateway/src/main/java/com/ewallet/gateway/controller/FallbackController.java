package com.ewallet.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/wallet")
    public ResponseEntity<Map<String, Object>> walletServiceFallback() {
        log.warn("Wallet service fallback triggered");
        return createFallbackResponse("Wallet Service");
    }

    @GetMapping("/payment")
    public ResponseEntity<Map<String, Object>> paymentServiceFallback() {
        log.warn("Payment service fallback triggered");
        return createFallbackResponse("Payment Service");
    }

    @GetMapping("/notification")
    public ResponseEntity<Map<String, Object>> notificationServiceFallback() {
        log.warn("Notification service fallback triggered");
        return createFallbackResponse("Notification Service");
    }

    @GetMapping("/merchant")
    public ResponseEntity<Map<String, Object>> merchantServiceFallback() {
        log.warn("Merchant service fallback triggered");
        return createFallbackResponse("Merchant Service");
    }

    @GetMapping("/ledger")
    public ResponseEntity<Map<String, Object>> ledgerServiceFallback() {
        log.warn("Ledger service fallback triggered");
        return createFallbackResponse("Ledger Service");
    }

    private ResponseEntity<Map<String, Object>> createFallbackResponse(String serviceName) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Service Unavailable");
        response.put("message", serviceName + " is currently unavailable. Please try again later.");
        response.put("service", serviceName);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
