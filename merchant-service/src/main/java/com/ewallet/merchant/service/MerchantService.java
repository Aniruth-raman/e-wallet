package com.ewallet.merchant.service;

import com.ewallet.merchant.dto.CreditRequest;
import com.ewallet.merchant.dto.DebitRequest;
import com.ewallet.merchant.dto.MerchantResponse;
import com.ewallet.merchant.dto.TransactionResponse;
import com.ewallet.merchant.entity.*;
import com.ewallet.merchant.exception.*;
import com.ewallet.merchant.repository.AuditLogRepository;
import com.ewallet.merchant.repository.MerchantRepository;
import com.ewallet.merchant.repository.MerchantTransactionRepository;
import com.ewallet.merchant.repository.MerchantWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MerchantService {
    
    private static final Logger logger = LoggerFactory.getLogger(MerchantService.class);
    
    private final MerchantRepository merchantRepository;
    private final MerchantWalletRepository merchantWalletRepository;
    private final MerchantTransactionRepository merchantTransactionRepository;
    private final AuditLogRepository auditLogRepository;
    
    public MerchantService(MerchantRepository merchantRepository,
                          MerchantWalletRepository merchantWalletRepository,
                          MerchantTransactionRepository merchantTransactionRepository,
                          AuditLogRepository auditLogRepository) {
        this.merchantRepository = merchantRepository;
        this.merchantWalletRepository = merchantWalletRepository;
        this.merchantTransactionRepository = merchantTransactionRepository;
        this.auditLogRepository = auditLogRepository;
    }
    
    public MerchantResponse getMerchantBalance(UUID merchantId, Currency currency) {
        logger.info("Fetching balance for merchant: {} with currency: {}", merchantId, currency);
        
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new MerchantNotFoundException(merchantId));
        
        MerchantWallet wallet = merchantWalletRepository.findByMerchantIdAndCurrency(merchantId, currency)
                .orElseThrow(() -> new MerchantWalletNotFoundException(merchantId, currency));
        
        logger.info("Retrieved balance: {} {} for merchant: {}", wallet.getBalance(), currency, merchantId);
        
        return new MerchantResponse(
                merchant.getId(),
                merchant.getName(),
                merchant.getEmail(),
                merchant.getBusinessType(),
                wallet.getAccountNumber(),
                wallet.getBalance(),
                wallet.getCurrency()
        );
    }
    
    @Transactional
    public TransactionResponse creditMerchant(CreditRequest request) {
        logger.info("Processing credit request for merchant: {}, amount: {}, currency: {}, transactionId: {}", 
                request.getMerchantId(), request.getAmount(), request.getCurrency(), request.getTransactionId());
        
        if (merchantTransactionRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
            throw new DuplicateTransactionException(request.getTransactionId());
        }
        
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new MerchantNotFoundException(request.getMerchantId()));
        
        MerchantWallet wallet = merchantWalletRepository.findByMerchantIdAndCurrency(
                request.getMerchantId(), request.getCurrency())
                .orElseThrow(() -> new MerchantWalletNotFoundException(
                        request.getMerchantId(), request.getCurrency()));
        
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = oldBalance.add(request.getAmount());
        wallet.setBalance(newBalance);
        
        MerchantWallet savedWallet = merchantWalletRepository.save(wallet);
        
        MerchantTransaction transaction = new MerchantTransaction();
        transaction.setMerchantWallet(wallet);
        transaction.setTransactionId(request.getTransactionId());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.CREDIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        MerchantTransaction savedTransaction = merchantTransactionRepository.save(transaction);
        
        AuditLog auditLog = new AuditLog();
        auditLog.setMerchantWalletId(wallet.getId());
        auditLog.setAction("CREDIT");
        auditLog.setOldBalance(oldBalance);
        auditLog.setNewBalance(newBalance);
        auditLog.setAmount(request.getAmount());
        auditLog.setTransactionId(request.getTransactionId());
        
        auditLogRepository.save(auditLog);
        
        logger.info("Credit successful for merchant: {}, new balance: {}, transactionId: {}", 
                request.getMerchantId(), newBalance, request.getTransactionId());
        
        return new TransactionResponse(
                savedTransaction.getTransactionId(),
                savedTransaction.getStatus(),
                savedTransaction.getAmount()
        );
    }
    
    @Transactional
    public TransactionResponse debitMerchant(DebitRequest request) {
        logger.info("Processing debit request for merchant: {}, amount: {}, transactionId: {}", 
                request.getMerchantId(), request.getAmount(), request.getTransactionId());
        
        MerchantTransaction existingTransaction = merchantTransactionRepository
                .findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new TransactionNotFoundException(request.getTransactionId()));
        
        if (existingTransaction.getType() != TransactionType.CREDIT) {
            throw new IllegalArgumentException("Can only debit a credit transaction");
        }
        
        MerchantWallet wallet = existingTransaction.getMerchantWallet();
        
        BigDecimal oldBalance = wallet.getBalance();
        
        if (oldBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(request.getAmount(), oldBalance);
        }
        
        BigDecimal newBalance = oldBalance.subtract(request.getAmount());
        wallet.setBalance(newBalance);
        
        merchantWalletRepository.save(wallet);
        
        String compensatingTransactionId = "COMP-" + request.getTransactionId();
        
        MerchantTransaction transaction = new MerchantTransaction();
        transaction.setMerchantWallet(wallet);
        transaction.setTransactionId(compensatingTransactionId);
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.DEBIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        
        MerchantTransaction savedTransaction = merchantTransactionRepository.save(transaction);
        
        AuditLog auditLog = new AuditLog();
        auditLog.setMerchantWalletId(wallet.getId());
        auditLog.setAction("DEBIT");
        auditLog.setOldBalance(oldBalance);
        auditLog.setNewBalance(newBalance);
        auditLog.setAmount(request.getAmount());
        auditLog.setTransactionId(compensatingTransactionId);
        
        auditLogRepository.save(auditLog);
        
        logger.info("Debit successful for merchant: {}, new balance: {}, compensatingTransactionId: {}", 
                request.getMerchantId(), newBalance, compensatingTransactionId);
        
        return new TransactionResponse(
                savedTransaction.getTransactionId(),
                savedTransaction.getStatus(),
                savedTransaction.getAmount()
        );
    }
    
    public TransactionResponse getTransactionStatus(String transactionId) {
        logger.info("Fetching transaction status for transactionId: {}", transactionId);
        
        MerchantTransaction transaction = merchantTransactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        
        return new TransactionResponse(
                transaction.getTransactionId(),
                transaction.getStatus(),
                transaction.getAmount()
        );
    }
}
