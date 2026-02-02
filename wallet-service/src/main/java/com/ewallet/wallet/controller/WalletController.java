package com.ewallet.wallet.controller;

import com.ewallet.wallet.dto.*;
import com.ewallet.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet management APIs")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{customerId}")
    @Operation(summary = "Get wallet by customer ID", description = "Retrieves wallet details for a given customer")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID customerId) {
        log.info("GET /api/wallet/{} - Fetching wallet for customer", customerId);
        WalletResponse response = walletService.getWalletByCustomerId(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate-balance")
    @Operation(summary = "Validate balance", description = "Checks if customer has sufficient balance")
    public ResponseEntity<Map<String, Boolean>> validateBalance(
            @Valid @RequestBody BalanceValidationRequest request) {
        log.info("POST /api/wallet/validate-balance - Validating balance for customer: {}", 
                request.getCustomerId());
        boolean isValid = walletService.validateBalance(request);
        return ResponseEntity.ok(Map.of("hasSufficientBalance", isValid));
    }

    @PostMapping("/reserve")
    @Operation(summary = "Reserve amount", description = "Reserves an amount from customer's wallet")
    public ResponseEntity<ReservationResponse> reserveAmount(
            @Valid @RequestBody ReservationRequest request) {
        log.info("POST /api/wallet/reserve - Reserving amount for customer: {}", 
                request.getCustomerId());
        ReservationResponse response = walletService.reserveAmount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/confirm-reservation/{reservationId}")
    @Operation(summary = "Confirm reservation", description = "Confirms a pending reservation")
    public ResponseEntity<ReservationResponse> confirmReservation(
            @PathVariable UUID reservationId) {
        log.info("POST /api/wallet/confirm-reservation/{} - Confirming reservation", reservationId);
        ReservationResponse response = walletService.confirmReservation(reservationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/release-reservation/{reservationId}")
    @Operation(summary = "Release reservation", description = "Releases a reservation and refunds the amount")
    public ResponseEntity<ReservationResponse> releaseReservation(
            @PathVariable UUID reservationId) {
        log.info("POST /api/wallet/release-reservation/{} - Releasing reservation", reservationId);
        ReservationResponse response = walletService.releaseReservation(reservationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/collect-fee")
    @Operation(summary = "Collect fee", description = "Collects fee from customer's wallet")
    public ResponseEntity<Map<String, String>> collectFee(
            @Valid @RequestBody FeeCollectionRequest request) {
        log.info("POST /api/wallet/collect-fee - Collecting fee for customer: {}", 
                request.getCustomerId());
        walletService.collectFee(request);
        return ResponseEntity.ok(Map.of("message", "Fee collected successfully"));
    }
}
