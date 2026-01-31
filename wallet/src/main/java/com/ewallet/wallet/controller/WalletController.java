package com.ewallet.wallet.controller;

import com.ewallet.wallet.dto.UserBalanceDTO;
import com.ewallet.wallet.dto.WalletRequestDTO;
import com.ewallet.wallet.entity.Wallet;
import com.ewallet.wallet.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @GetMapping("check-balance/{userId}")
    public UserBalanceDTO getUserWithBalance(@PathVariable("userId") String userId) {

        return walletService.getUserWithBalance(userId);

    }

    @PostMapping("/credit")
    public ResponseEntity<String> credit(@RequestBody WalletRequestDTO request) {

        walletService.credit(request);

        return ResponseEntity.ok("Amount credited successfully");

    }

    @PostMapping("/debit")
    public ResponseEntity<String> debit(@RequestBody WalletRequestDTO request) {

        walletService.debit(request);

        return ResponseEntity.ok("Amount debited successfully");
    }

    @GetMapping("/{accNo}/transactions")
    public List<Wallet> viewTransactions(@PathVariable("accNo") String walletAccNo) {

        return walletService.viewTransactions(walletAccNo);
    }
}
