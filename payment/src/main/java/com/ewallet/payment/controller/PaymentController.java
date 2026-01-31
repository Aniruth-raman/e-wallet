package com.ewallet.payment.controller;

import com.ewallet.payment.dto.PaymentRequest;
import com.ewallet.payment.dto.TransactionResponse;
import com.ewallet.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/payment")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    // expose POST /api/v1/payment/process
    @PostMapping("/process")
    public ResponseEntity<TransactionResponse> process(@Valid @RequestBody PaymentRequest req) {
        return ResponseEntity.ok(paymentService.processPayment(req));
    }
}
