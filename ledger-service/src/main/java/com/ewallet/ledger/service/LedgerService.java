package com.ewallet.ledger.service;

import com.ewallet.ledger.dto.LedgerEntryRequest;
import com.ewallet.ledger.dto.LedgerEntryResponse;
import com.ewallet.ledger.dto.TransactionHistoryResponse;
import com.ewallet.ledger.entity.AuditLog;
import com.ewallet.ledger.entity.LedgerEntry;
import com.ewallet.ledger.exception.DuplicateTransactionException;
import com.ewallet.ledger.exception.InvalidTransactionStateException;
import com.ewallet.ledger.exception.LedgerEntryNotFoundException;
import com.ewallet.ledger.repository.AuditLogRepository;
import com.ewallet.ledger.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public LedgerEntryResponse createLedgerEntry(LedgerEntryRequest request) {
        log.info("Creating ledger entry for transaction: {}", request.getTransactionId());

        if (ledgerEntryRepository.existsByTransactionId(request.getTransactionId())) {
            throw new DuplicateTransactionException(
                    "Transaction with ID " + request.getTransactionId() + " already exists");
        }

        LedgerEntry ledgerEntry = LedgerEntry.builder()
                .transactionId(request.getTransactionId())
                .customerId(request.getCustomerId())
                .merchantId(request.getMerchantId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .transactionType(request.getTransactionType())
                .status(LedgerEntry.Status.COMPLETED)
                .customerBalanceBefore(request.getCustomerBalanceBefore())
                .customerBalanceAfter(request.getCustomerBalanceAfter())
                .merchantBalanceBefore(request.getMerchantBalanceBefore())
                .merchantBalanceAfter(request.getMerchantBalanceAfter())
                .productName(request.getProductName())
                .productDescription(request.getProductDescription())
                .build();

        LedgerEntry savedEntry = ledgerEntryRepository.save(ledgerEntry);

        createAuditLog(savedEntry.getId(), "ENTRY_CREATED",
                String.format("Ledger entry created for transaction %s with amount %s %s",
                        savedEntry.getTransactionId(), savedEntry.getAmount(), savedEntry.getCurrency()));

        log.info("Ledger entry created successfully with ID: {}", savedEntry.getId());
        return LedgerEntryResponse.from(savedEntry);
    }

    @Transactional(readOnly = true)
    public LedgerEntryResponse getLedgerEntry(String transactionId) {
        log.info("Retrieving ledger entry for transaction: {}", transactionId);

        LedgerEntry ledgerEntry = ledgerEntryRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new LedgerEntryNotFoundException(
                        "Ledger entry not found for transaction: " + transactionId));

        return LedgerEntryResponse.from(ledgerEntry);
    }

    @Transactional
    public LedgerEntryResponse reverseLedgerEntry(String transactionId) {
        log.info("Reversing ledger entry for transaction: {}", transactionId);

        LedgerEntry ledgerEntry = ledgerEntryRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new LedgerEntryNotFoundException(
                        "Ledger entry not found for transaction: " + transactionId));

        if (ledgerEntry.getStatus() == LedgerEntry.Status.REVERSED) {
            throw new InvalidTransactionStateException(
                    "Transaction " + transactionId + " is already reversed");
        }

        if (ledgerEntry.getStatus() == LedgerEntry.Status.PENDING) {
            throw new InvalidTransactionStateException(
                    "Cannot reverse pending transaction " + transactionId);
        }

        ledgerEntry.setStatus(LedgerEntry.Status.REVERSED);
        LedgerEntry updatedEntry = ledgerEntryRepository.save(ledgerEntry);

        createAuditLog(updatedEntry.getId(), "ENTRY_REVERSED",
                String.format("Ledger entry reversed for transaction %s", transactionId));

        log.info("Ledger entry reversed successfully for transaction: {}", transactionId);
        return LedgerEntryResponse.from(updatedEntry);
    }

    @Transactional(readOnly = true)
    public TransactionHistoryResponse getCustomerTransactions(String customerId, Pageable pageable) {
        log.info("Retrieving transaction history for customer: {}", customerId);

        Page<LedgerEntry> page = ledgerEntryRepository.findByCustomerId(customerId, pageable);

        List<LedgerEntryResponse> transactions = page.getContent().stream()
                .map(LedgerEntryResponse::from)
                .collect(Collectors.toList());

        return TransactionHistoryResponse.builder()
                .transactions(transactions)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionHistoryResponse getMerchantTransactions(String merchantId, Pageable pageable) {
        log.info("Retrieving transaction history for merchant: {}", merchantId);

        Page<LedgerEntry> page = ledgerEntryRepository.findByMerchantId(merchantId, pageable);

        List<LedgerEntryResponse> transactions = page.getContent().stream()
                .map(LedgerEntryResponse::from)
                .collect(Collectors.toList());

        return TransactionHistoryResponse.builder()
                .transactions(transactions)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }

    private void createAuditLog(java.util.UUID ledgerEntryId, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .ledgerEntryId(ledgerEntryId)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: action={}, ledgerEntryId={}", action, ledgerEntryId);
    }
}
