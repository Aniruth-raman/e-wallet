package com.ewallet.payment.service;

import com.ewallet.payment.dto.BalanceResponse;
import com.ewallet.payment.dto.NotificationRequest;
import com.ewallet.payment.dto.PaymentRequest;
import com.ewallet.payment.dto.TransactionResponse;
import com.ewallet.payment.exception.DebitFailedException;
import com.ewallet.payment.exception.InsufficientFundException;
import com.ewallet.payment.exception.ServiceUnavailableException;
import com.ewallet.payment.model.Ledger;
import com.ewallet.payment.repository.LedgerRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Log4j2
public class PaymentService {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LedgerRepository ledgerRepository;

    @Transactional
    public TransactionResponse processPayment(PaymentRequest req) {
        BalanceResponse balanceResponse = getBalanceDetails(req);
        boolean sufficient = validateTransaction(req, balanceResponse);
        if (!sufficient) {
            throw new InsufficientFundException("The current wallet amount %s is insufficient for this transaction".formatted(req.getAmount()));
        }

        BigDecimal fee = calculateWalletFee(req.getAmount());
        BigDecimal netAmount = req.getAmount().subtract(fee);

        try {
            Map<String, Object> debitReq = Map.of("userId", req.getCustomerId(), "amount", req.getAmount());
            restTemplate.postForObject("http://localhost:8081/api/wallet/debit", debitReq, Void.class);
        } catch (RestClientException ex) {
            throw new DebitFailedException("Failed to debit wallet: " + ex.getMessage(), ex);
        }

        try {
            Map<String, Object> creditMerchantReq = Map.of("userId", req.getMerchantAccountNo(), "amount", netAmount);
            restTemplate.postForObject("http://localhost:8081/api/wallet/credit", creditMerchantReq, Void.class);

            Map<String, Object> creditFeeReq = Map.of("userId", getWalletFeeAccountId(), "amount", fee);
            restTemplate.postForObject("http://localhost:8081/api/wallet/credit", creditFeeReq, Void.class);
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to credit accounts: " + ex.getMessage(), ex);
        }

        String transactionId = UUID.randomUUID().toString();

        Ledger customerLedger = new Ledger();
        customerLedger.setUserId(req.getCustomerId());
        customerLedger.setAmount(req.getAmount());
        customerLedger.setStatus("DEBITED");
        customerLedger.setTransactionId(transactionId);
        customerLedger.setCreatedAt(Instant.now());

        Ledger merchantLedger = new Ledger();
        merchantLedger.setUserId(req.getMerchantAccountNo());
        merchantLedger.setAmount(netAmount);
        merchantLedger.setStatus("CREDITED");
        merchantLedger.setTransactionId(transactionId);
        merchantLedger.setCreatedAt(Instant.now());

        Ledger feeLedger = new Ledger();
        feeLedger.setUserId(getWalletFeeAccountId());
        feeLedger.setAmount(fee);
        feeLedger.setStatus("CREDITED");
        feeLedger.setTransactionId(transactionId);
        feeLedger.setCreatedAt(Instant.now());

        ledgerRepository.saveAll(List.of(customerLedger, merchantLedger, feeLedger));
        try {
            NotificationRequest customerNotification = new NotificationRequest();
            customerNotification.setUserId(req.getCustomerId().toString());
            customerNotification.setTransactionStatus("SUCCESS");
            customerNotification.setAmount(req.getAmount().doubleValue());
            customerNotification.setMessage("Your payment of " + req.getAmount() + " was successful.");
            restTemplate.postForEntity("http://localhost:8083/notifications/send", customerNotification, Void.class);

            NotificationRequest merchantNotification = new NotificationRequest();
            merchantNotification.setUserId(req.getMerchantAccountNo().toString());
            merchantNotification.setTransactionStatus("SUCCESS");
            merchantNotification.setAmount(netAmount.doubleValue());
            merchantNotification.setMessage("You have received a payment of " + netAmount + ".");
            restTemplate.postForEntity("http://localhost:8083/notifications/send", merchantNotification, Void.class);
        } catch (RestClientException ex) {
            // Notification failures shouldn't prevent transaction success; log and continue
            log.warn("Failed to send notification(s) for transaction {}: {}", transactionId, ex.getMessage());
        }
        // 6) Return response
        return new TransactionResponse("SUCCESS", transactionId);
    }

    private boolean validateTransaction(PaymentRequest req, BalanceResponse resp) {
        return resp.getBalance().compareTo(req.getAmount()) >= 0 && resp.getCurrency().equals(req.getProductCurrency());
    }

    private BalanceResponse getBalanceDetails(PaymentRequest req) {
        BalanceResponse resp = restTemplate.getForObject("http://localhost:8081/api/wallet/check-balance/" + req.getCustomerId(), BalanceResponse.class);
        if (resp == null) {
            throw new ServiceUnavailableException("No response from wallet service");
        }
        return resp;
    }

    private static final BigDecimal WALLET_FEE_PERCENT = new BigDecimal("0.02");
    private static final Long WALLET_FEE_ACCOUNT = 1234567890L;

    private BigDecimal calculateWalletFee(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.multiply(WALLET_FEE_PERCENT).setScale(2, RoundingMode.HALF_UP);
    }

    private Long getWalletFeeAccountId() {
        return WALLET_FEE_ACCOUNT;
    }
}
