package com.ewallet.wallet.service;

import com.ewallet.wallet.config.RedisLockService;
import com.ewallet.wallet.dto.*;
import com.ewallet.wallet.entity.*;
import com.ewallet.wallet.exception.*;
import com.ewallet.wallet.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final WalletReservationRepository reservationRepository;
    private final AuditLogRepository auditLogRepository;
    private final RedisLockService redisLockService;

    @Transactional(readOnly = true)
    public WalletResponse getWalletByCustomerId(UUID customerId) {
        log.info("Fetching wallet for customer ID: {}", customerId);
        
        Wallet wallet = walletRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found for customer ID: " + customerId));

        return WalletResponse.builder()
                .walletId(wallet.getId())
                .customerId(wallet.getCustomer().getId())
                .customerName(wallet.getCustomer().getName())
                .customerEmail(wallet.getCustomer().getEmail())
                .accountNumber(wallet.getAccountNumber())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean validateBalance(BalanceValidationRequest request) {
        log.info("Validating balance for customer ID: {}, amount: {}, currency: {}", 
                request.getCustomerId(), request.getAmount(), request.getCurrency());

        Wallet wallet = walletRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new WalletNotFoundException(
                        "Wallet not found for customer ID: " + request.getCustomerId()));

        if (!wallet.getCurrency().equals(request.getCurrency())) {
            throw new InvalidCurrencyException(
                    "Currency mismatch. Wallet currency: " + wallet.getCurrency() + 
                    ", Requested currency: " + request.getCurrency());
        }

        boolean hasSufficientBalance = wallet.getBalance().compareTo(request.getAmount()) >= 0;
        log.info("Balance validation result: {}", hasSufficientBalance);
        
        return hasSufficientBalance;
    }

    @Transactional
    public ReservationResponse reserveAmount(ReservationRequest request) {
        log.info("Reserving amount for customer ID: {}, amount: {}, transactionId: {}", 
                request.getCustomerId(), request.getAmount(), request.getTransactionId());

        String lockKey = "customer:" + request.getCustomerId();
        
        if (!redisLockService.tryLock(lockKey)) {
            log.warn("Failed to acquire lock for customer ID: {}", request.getCustomerId());
            throw new LockAcquisitionException(
                    "Unable to acquire lock for wallet operation. Please try again.");
        }

        try {
            Wallet wallet = walletRepository.findByCustomerId(request.getCustomerId())
                    .orElseThrow(() -> new WalletNotFoundException(
                            "Wallet not found for customer ID: " + request.getCustomerId()));

            if (!wallet.getCurrency().equals(request.getCurrency())) {
                throw new InvalidCurrencyException(
                        "Currency mismatch. Wallet currency: " + wallet.getCurrency() + 
                        ", Requested currency: " + request.getCurrency());
            }

            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance. Available: " + wallet.getBalance() + 
                        ", Requested: " + request.getAmount());
            }

            BigDecimal oldBalance = wallet.getBalance();
            BigDecimal newBalance = oldBalance.subtract(request.getAmount());
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            WalletReservation reservation = new WalletReservation();
            reservation.setWallet(wallet);
            reservation.setTransactionId(request.getTransactionId());
            reservation.setAmount(request.getAmount());
            reservation.setStatus(ReservationStatus.PENDING);
            reservation = reservationRepository.save(reservation);

            logAudit(wallet.getId(), "RESERVE_AMOUNT", oldBalance, newBalance, 
                    request.getAmount(), request.getTransactionId());

            log.info("Successfully reserved amount. Reservation ID: {}", reservation.getId());

            return ReservationResponse.builder()
                    .reservationId(reservation.getId())
                    .status(ReservationStatus.PENDING)
                    .message("Amount reserved successfully")
                    .build();

        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @Transactional
    public ReservationResponse confirmReservation(UUID reservationId) {
        log.info("Confirming reservation ID: {}", reservationId);

        WalletReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found for ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot confirm reservation with status: " + reservation.getStatus());
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);

        logAudit(reservation.getWallet().getId(), "CONFIRM_RESERVATION", null, null, 
                reservation.getAmount(), reservation.getTransactionId());

        log.info("Successfully confirmed reservation ID: {}", reservationId);

        return ReservationResponse.builder()
                .reservationId(reservationId)
                .status(ReservationStatus.CONFIRMED)
                .message("Reservation confirmed successfully")
                .build();
    }

    @Transactional
    public ReservationResponse releaseReservation(UUID reservationId) {
        log.info("Releasing reservation ID: {}", reservationId);

        WalletReservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(
                        "Reservation not found for ID: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.RELEASED) {
            log.warn("Reservation already released: {}", reservationId);
            return ReservationResponse.builder()
                    .reservationId(reservationId)
                    .status(ReservationStatus.RELEASED)
                    .message("Reservation already released")
                    .build();
        }

        Wallet wallet = reservation.getWallet();
        String lockKey = "customer:" + wallet.getCustomer().getId();
        
        if (!redisLockService.tryLock(lockKey)) {
            throw new LockAcquisitionException(
                    "Unable to acquire lock for wallet operation. Please try again.");
        }

        try {
            BigDecimal oldBalance = wallet.getBalance();
            BigDecimal newBalance = oldBalance.add(reservation.getAmount());
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            reservation.setStatus(ReservationStatus.RELEASED);
            reservationRepository.save(reservation);

            logAudit(wallet.getId(), "RELEASE_RESERVATION", oldBalance, newBalance, 
                    reservation.getAmount(), reservation.getTransactionId());

            log.info("Successfully released reservation ID: {}", reservationId);

            return ReservationResponse.builder()
                    .reservationId(reservationId)
                    .status(ReservationStatus.RELEASED)
                    .message("Reservation released and amount refunded")
                    .build();

        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    @Transactional
    public void collectFee(FeeCollectionRequest request) {
        log.info("Collecting fee for customer ID: {}, amount: {}, transactionId: {}", 
                request.getCustomerId(), request.getAmount(), request.getTransactionId());

        String lockKey = "customer:" + request.getCustomerId();
        
        if (!redisLockService.tryLock(lockKey)) {
            throw new LockAcquisitionException(
                    "Unable to acquire lock for wallet operation. Please try again.");
        }

        try {
            Wallet wallet = walletRepository.findByCustomerId(request.getCustomerId())
                    .orElseThrow(() -> new WalletNotFoundException(
                            "Wallet not found for customer ID: " + request.getCustomerId()));

            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Insufficient balance for fee collection. Available: " + wallet.getBalance() + 
                        ", Required: " + request.getAmount());
            }

            BigDecimal oldBalance = wallet.getBalance();
            BigDecimal newBalance = oldBalance.subtract(request.getAmount());
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            logAudit(wallet.getId(), "COLLECT_FEE", oldBalance, newBalance, 
                    request.getAmount(), request.getTransactionId());

            log.info("Successfully collected fee from customer ID: {}", request.getCustomerId());

        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }

    private void logAudit(UUID walletId, String action, BigDecimal oldBalance, 
                         BigDecimal newBalance, BigDecimal amount, String transactionId) {
        AuditLog auditLog = new AuditLog();
        auditLog.setWalletId(walletId);
        auditLog.setAction(action);
        auditLog.setOldBalance(oldBalance);
        auditLog.setNewBalance(newBalance);
        auditLog.setAmount(amount);
        auditLog.setTransactionId(transactionId);
        auditLogRepository.save(auditLog);
        log.debug("Audit log created for wallet ID: {}, action: {}", walletId, action);
    }
}
