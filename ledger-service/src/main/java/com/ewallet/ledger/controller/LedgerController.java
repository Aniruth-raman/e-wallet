package com.ewallet.ledger.controller;

import com.ewallet.ledger.dto.LedgerEntryRequest;
import com.ewallet.ledger.dto.LedgerEntryResponse;
import com.ewallet.ledger.dto.TransactionHistoryResponse;
import com.ewallet.ledger.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger", description = "Ledger management APIs")
public class LedgerController {

    private final LedgerService ledgerService;

    @PostMapping("/transactions")
    @Operation(summary = "Create ledger entry", description = "Creates a new ledger entry for a transaction")
    public ResponseEntity<LedgerEntryResponse> createLedgerEntry(
            @Valid @RequestBody LedgerEntryRequest request) {
        LedgerEntryResponse response = ledgerService.createLedgerEntry(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/transactions/{transactionId}")
    @Operation(summary = "Get ledger entry", description = "Retrieves a ledger entry by transaction ID")
    public ResponseEntity<LedgerEntryResponse> getLedgerEntry(
            @PathVariable String transactionId) {
        LedgerEntryResponse response = ledgerService.getLedgerEntry(transactionId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transactions/{transactionId}")
    @Operation(summary = "Reverse ledger entry", description = "Reverses a ledger entry (compensating transaction)")
    public ResponseEntity<LedgerEntryResponse> reverseLedgerEntry(
            @PathVariable String transactionId) {
        LedgerEntryResponse response = ledgerService.reverseLedgerEntry(transactionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerId}/transactions")
    @Operation(summary = "Get customer transaction history", description = "Retrieves transaction history for a customer")
    public ResponseEntity<TransactionHistoryResponse> getCustomerTransactions(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        TransactionHistoryResponse response = ledgerService.getCustomerTransactions(customerId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/merchants/{merchantId}/transactions")
    @Operation(summary = "Get merchant transaction history", description = "Retrieves transaction history for a merchant")
    public ResponseEntity<TransactionHistoryResponse> getMerchantTransactions(
            @PathVariable String merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        
        TransactionHistoryResponse response = ledgerService.getMerchantTransactions(merchantId, pageable);
        return ResponseEntity.ok(response);
    }
}
