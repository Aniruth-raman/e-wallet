package com.ewallet.wallet.service;

import com.ewallet.wallet.dto.UserBalanceDTO;
import com.ewallet.wallet.dto.WalletRequestDTO;
import com.ewallet.wallet.entity.Wallet;
import com.ewallet.wallet.entity.WalletUser;
import com.ewallet.wallet.exception.InsufficientBalanceException;
import com.ewallet.wallet.repository.UserRepository;
import com.ewallet.wallet.repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    public UserBalanceDTO getUserWithBalance(String userId) {

        WalletUser user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal balance = walletRepository.checkBalance(user.getWalletAccNo());

        return new UserBalanceDTO(user.getUserId(), user.getWalletAccNo(), balance, user.getCurrency());

    }

    public List<Wallet> viewTransactions(String walletAccNo) {
        return walletRepository.findByWalletAccNoOrderByCreatedTimeAsc(walletAccNo);
    }

    public void credit(WalletRequestDTO request) {

        WalletUser user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet = new Wallet(user.getWalletAccNo(), "C", request.getAmount(), LocalDateTime.now());

        walletRepository.save(wallet);

    }

    public void debit(WalletRequestDTO request) {

        WalletUser user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal balance = walletRepository.checkBalance(user.getWalletAccNo());

        if (balance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        Wallet wallet = new Wallet(user.getWalletAccNo(), "D", request.getAmount(), LocalDateTime.now());

        walletRepository.save(wallet);

    }
}
