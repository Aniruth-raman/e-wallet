package com.ewallet.wallet.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
@RestController
@RequestMapping("/api/v1/wallet")
=======
import com.ewallet.wallet.dto.UserBalanceDTO;
import com.ewallet.wallet.dto.WalletRequestDTO;
import com.ewallet.wallet.entity.Wallet;
import com.ewallet.wallet.service.WalletService;

@RestController
@RequestMapping("/api/v1/")
>>>>>>> 97ca1f4 (commit 2)
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
