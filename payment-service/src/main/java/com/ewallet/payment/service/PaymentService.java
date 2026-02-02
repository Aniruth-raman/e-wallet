package com.ewallet.payment.service;

import com.ewallet.payment.client.*;
import com.ewallet.payment.dto.*;
import com.ewallet.payment.entity.AuditLog;
import com.ewallet.payment.entity.PaymentTransaction;
import com.ewallet.payment.entity.SagaStep;
import com.ewallet.payment.enums.PaymentStatus;
import com.ewallet.payment.enums.StepStatus;
import com.ewallet.payment.exception.DuplicateTransactionException;
import com.ewallet.payment.exception.PaymentNotFoundException;
import com.ewallet.payment.exception.SagaFailureException;
import com.ewallet.payment.repository.AuditLogRepository;
import com.ewallet.payment.repository.PaymentTransactionRepository;
import com.ewallet.payment.repository.SagaStepRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.01"); // 1%
    
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SagaStepRepository sagaStepRepository;
    private final AuditLogRepository auditLogRepository;
    
    private final WalletServiceClient walletServiceClient;
    private final MerchantServiceClient merchantServiceClient;
    private final LedgerServiceClient ledgerServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {
        String transactionId = request.getTransactionId() != null ? 
                request.getTransactionId() : UUID.randomUUID().toString();
        
        // Idempotency check
        if (paymentTransactionRepository.existsByTransactionId(transactionId)) {
            throw new DuplicateTransactionException("Transaction already exists: " + transactionId);
        }
        
        log.info("Initiating payment for transaction: {}", transactionId);
        
        // Create payment transaction
        PaymentTransaction payment = PaymentTransaction.builder()
                .transactionId(transactionId)
                .customerId(request.getCustomerId())
                .merchantId(request.getMerchantId())
                .productName(request.getProduct().getName())
                .productDescription(request.getProduct().getDescription())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.INITIATED)
                .currentStep("INITIATED")
                .build();
        
        payment = paymentTransactionRepository.save(payment);
        logAudit(transactionId, "PAYMENT_INITIATED", null, PaymentStatus.INITIATED.name(), 
                "Payment initiated with amount: " + request.getAmount());
        
        // Execute saga asynchronously
        executeSagaAsync(payment);
        
        return PaymentResponse.builder()
                .transactionId(transactionId)
                .status(PaymentStatus.INITIATED)
                .message("Payment initiated successfully")
                .build();
    }
    
    private void executeSagaAsync(PaymentTransaction payment) {
        CompletableFuture.runAsync(() -> {
            try {
                executeSaga(payment);
            } catch (Exception e) {
                log.error("Saga execution failed for transaction: {}", payment.getTransactionId(), e);
                handleSagaFailure(payment, e);
            }
        });
    }
    
    private void executeSaga(PaymentTransaction payment) {
        try {
            // Step 1: Validate balance
            executeStep(payment, "VALIDATE_BALANCE", () -> {
                BigDecimal balance = walletServiceClient.validateBalance(payment.getCustomerId());
                if (balance.compareTo(payment.getAmount()) < 0) {
                    throw new SagaFailureException("Insufficient balance");
                }
            });
            
            // Step 2: Reserve wallet amount
            executeStep(payment, "RESERVE_WALLET", () -> {
                WalletReservationRequest reserveRequest = WalletReservationRequest.builder()
                        .customerId(payment.getCustomerId())
                        .amount(payment.getAmount())
                        .transactionId(payment.getTransactionId())
                        .build();
                walletServiceClient.reserve(reserveRequest);
                updatePaymentStatus(payment, PaymentStatus.WALLET_RESERVED, "RESERVE_WALLET");
            });
            
            // Step 3: Credit merchant
            executeStep(payment, "CREDIT_MERCHANT", () -> {
                MerchantCreditRequest creditRequest = MerchantCreditRequest.builder()
                        .merchantId(payment.getMerchantId())
                        .amount(payment.getAmount())
                        .transactionId(payment.getTransactionId())
                        .build();
                merchantServiceClient.creditMerchant(creditRequest);
                updatePaymentStatus(payment, PaymentStatus.MERCHANT_CREDITED, "CREDIT_MERCHANT");
            });
            
            // Step 4: Update ledger
            executeStep(payment, "UPDATE_LEDGER", () -> {
                LedgerEntryRequest ledgerRequest = LedgerEntryRequest.builder()
                        .transactionId(payment.getTransactionId())
                        .customerId(payment.getCustomerId())
                        .merchantId(payment.getMerchantId())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency().name())
                        .type("PAYMENT")
                        .build();
                ledgerServiceClient.createLedgerEntry(ledgerRequest);
                updatePaymentStatus(payment, PaymentStatus.LEDGER_UPDATED, "UPDATE_LEDGER");
            });
            
            // Step 5: Send notifications
            executeStep(payment, "SEND_NOTIFICATIONS", () -> {
                // Notify customer
                NotificationRequest customerNotification = NotificationRequest.builder()
                        .recipient(payment.getCustomerId())
                        .type("PAYMENT_SUCCESS")
                        .subject("Payment Successful")
                        .message("Your payment of " + payment.getAmount() + " " + payment.getCurrency() + 
                                " to " + payment.getMerchantId() + " was successful.")
                        .build();
                notificationServiceClient.notifyCustomer(customerNotification);
                
                // Notify merchant
                NotificationRequest merchantNotification = NotificationRequest.builder()
                        .recipient(payment.getMerchantId())
                        .type("PAYMENT_RECEIVED")
                        .subject("Payment Received")
                        .message("You received a payment of " + payment.getAmount() + " " + 
                                payment.getCurrency() + " from " + payment.getCustomerId())
                        .build();
                notificationServiceClient.notifyMerchant(merchantNotification);
                
                updatePaymentStatus(payment, PaymentStatus.NOTIFICATION_SENT, "SEND_NOTIFICATIONS");
            });
            
            // Step 6: Collect fee
            executeStep(payment, "COLLECT_FEE", () -> {
                BigDecimal feeAmount = payment.getAmount().multiply(FEE_PERCENTAGE)
                        .setScale(2, RoundingMode.HALF_UP);
                WalletReservationRequest feeRequest = WalletReservationRequest.builder()
                        .customerId(payment.getCustomerId())
                        .amount(feeAmount)
                        .transactionId(payment.getTransactionId() + "-FEE")
                        .build();
                walletServiceClient.collectFee(feeRequest);
                updatePaymentStatus(payment, PaymentStatus.FEE_COLLECTED, "COLLECT_FEE");
            });
            
            // Confirm wallet reservation
            walletServiceClient.confirmReservation(payment.getTransactionId());
            
            // Mark as completed
            updatePaymentStatus(payment, PaymentStatus.COMPLETED, "COMPLETED");
            log.info("Payment saga completed successfully for transaction: {}", payment.getTransactionId());
            
        } catch (Exception e) {
            throw new SagaFailureException("Saga execution failed", e);
        }
    }
    
    private void executeStep(PaymentTransaction payment, String stepName, Runnable action) {
        SagaStep step = SagaStep.builder()
                .paymentTransaction(payment)
                .stepName(stepName)
                .status(StepStatus.PENDING)
                .attempt(0)
                .build();
        step = sagaStepRepository.save(step);
        
        int attempts = 0;
        Exception lastException = null;
        
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                attempts++;
                step.setAttempt(attempts);
                sagaStepRepository.save(step);
                
                log.info("Executing step {} for transaction: {} (attempt {}/{})", 
                        stepName, payment.getTransactionId(), attempts, MAX_RETRY_ATTEMPTS);
                
                action.run();
                
                step.setStatus(StepStatus.COMPLETED);
                sagaStepRepository.save(step);
                
                logAudit(payment.getTransactionId(), stepName + "_COMPLETED", 
                        payment.getStatus().name(), payment.getStatus().name(),
                        "Step completed successfully");
                
                return;
                
            } catch (Exception e) {
                lastException = e;
                log.error("Step {} failed for transaction: {} (attempt {}/{}): {}", 
                        stepName, payment.getTransactionId(), attempts, MAX_RETRY_ATTEMPTS, e.getMessage());
                
                if (attempts >= MAX_RETRY_ATTEMPTS) {
                    step.setStatus(StepStatus.FAILED);
                    step.setErrorMessage(e.getMessage());
                    sagaStepRepository.save(step);
                    
                    logAudit(payment.getTransactionId(), stepName + "_FAILED", 
                            payment.getStatus().name(), PaymentStatus.FAILED.name(),
                            "Step failed after " + MAX_RETRY_ATTEMPTS + " attempts: " + e.getMessage());
                    
                    throw new SagaFailureException("Step " + stepName + " failed after " + 
                            MAX_RETRY_ATTEMPTS + " attempts", e);
                }
                
                try {
                    Thread.sleep(1000 * attempts); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new SagaFailureException("Step interrupted", ie);
                }
            }
        }
        
        throw new SagaFailureException("Step " + stepName + " failed", lastException);
    }
    
    private void handleSagaFailure(PaymentTransaction payment, Exception e) {
        log.error("Handling saga failure for transaction: {}", payment.getTransactionId());
        
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(e.getMessage());
        paymentTransactionRepository.save(payment);
        
        logAudit(payment.getTransactionId(), "SAGA_FAILED", 
                payment.getStatus().name(), PaymentStatus.FAILED.name(),
                "Saga failed: " + e.getMessage());
        
        // Execute compensation
        executeCompensation(payment.getTransactionId());
    }
    
    @Transactional
    public void executeCompensation(String transactionId) {
        log.info("Executing compensation for transaction: {}", transactionId);
        
        PaymentTransaction payment = paymentTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + transactionId));
        
        logAudit(transactionId, "COMPENSATION_STARTED", 
                payment.getStatus().name(), payment.getStatus().name(),
                "Starting compensation process");
        
        try {
            // Compensate in reverse order
            if (payment.getStatus().ordinal() >= PaymentStatus.MERCHANT_CREDITED.ordinal()) {
                compensateStep(payment, "DEBIT_MERCHANT", () -> {
                    MerchantCreditRequest debitRequest = MerchantCreditRequest.builder()
                            .merchantId(payment.getMerchantId())
                            .amount(payment.getAmount())
                            .transactionId(payment.getTransactionId())
                            .build();
                    merchantServiceClient.debitMerchant(debitRequest);
                });
            }
            
            if (payment.getStatus().ordinal() >= PaymentStatus.LEDGER_UPDATED.ordinal()) {
                compensateStep(payment, "REVERSE_LEDGER", () -> {
                    ledgerServiceClient.reverseLedgerEntry(payment.getTransactionId());
                });
            }
            
            if (payment.getStatus().ordinal() >= PaymentStatus.WALLET_RESERVED.ordinal()) {
                compensateStep(payment, "RELEASE_RESERVATION", () -> {
                    walletServiceClient.releaseReservation(payment.getTransactionId());
                });
            }
            
            logAudit(transactionId, "COMPENSATION_COMPLETED", 
                    payment.getStatus().name(), PaymentStatus.FAILED.name(),
                    "Compensation completed successfully");
            
        } catch (Exception e) {
            log.error("Compensation failed for transaction: {}", transactionId, e);
            logAudit(transactionId, "COMPENSATION_FAILED", 
                    payment.getStatus().name(), payment.getStatus().name(),
                    "Compensation failed: " + e.getMessage());
        }
    }
    
    private void compensateStep(PaymentTransaction payment, String stepName, Runnable action) {
        SagaStep step = SagaStep.builder()
                .paymentTransaction(payment)
                .stepName(stepName)
                .status(StepStatus.COMPENSATING)
                .attempt(0)
                .build();
        step = sagaStepRepository.save(step);
        
        try {
            log.info("Compensating step {} for transaction: {}", stepName, payment.getTransactionId());
            action.run();
            
            step.setStatus(StepStatus.COMPENSATED);
            sagaStepRepository.save(step);
            
            logAudit(payment.getTransactionId(), stepName + "_COMPENSATED", 
                    payment.getStatus().name(), payment.getStatus().name(),
                    "Compensation step completed");
            
        } catch (Exception e) {
            log.error("Compensation step {} failed for transaction: {}: {}", 
                    stepName, payment.getTransactionId(), e.getMessage());
            
            step.setStatus(StepStatus.FAILED);
            step.setErrorMessage(e.getMessage());
            sagaStepRepository.save(step);
            
            throw new SagaFailureException("Compensation step failed: " + stepName, e);
        }
    }
    
    private void updatePaymentStatus(PaymentTransaction payment, PaymentStatus newStatus, String currentStep) {
        PaymentStatus oldStatus = payment.getStatus();
        payment.setStatus(newStatus);
        payment.setCurrentStep(currentStep);
        paymentTransactionRepository.save(payment);
        
        logAudit(payment.getTransactionId(), "STATUS_UPDATED", 
                oldStatus.name(), newStatus.name(),
                "Status updated to " + newStatus);
    }
    
    private void logAudit(String transactionId, String action, String fromStatus, 
                         String toStatus, String details) {
        AuditLog auditLog = AuditLog.builder()
                .paymentTransactionId(transactionId)
                .action(action)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .details(details)
                .build();
        auditLogRepository.save(auditLog);
    }
    
    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(String transactionId) {
        PaymentTransaction payment = paymentTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + transactionId));
        
        return PaymentStatusResponse.builder()
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .currentStep(payment.getCurrentStep())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
