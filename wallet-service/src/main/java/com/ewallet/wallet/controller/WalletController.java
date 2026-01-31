package com.ewallet.wallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample REST controller for Wallet Service
 */
@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @GetMapping("/status")
    public String getStatus() {
        return "Wallet Service is running";
    }
}
