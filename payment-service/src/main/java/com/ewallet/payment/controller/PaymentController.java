package com.ewallet.payment.controller;

import com.ewallet.payment.dto.PaymentRequest;
import com.ewallet.payment.dto.PaymentResponse;
import com.ewallet.payment.dto.PaymentStatusResponse;
import com.ewallet.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        log.info("Received payment request for customer: {} to merchant: {}", 
                request.getCustomerId(), request.getMerchantId());
        PaymentResponse response = paymentService.initiatePayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{transactionId}/status")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(@PathVariable String transactionId) {
        log.info("Fetching payment status for transaction: {}", transactionId);
        PaymentStatusResponse response = paymentService.getPaymentStatus(transactionId);
        return ResponseEntity.ok(response);
    }
}
