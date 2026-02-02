package com.ewallet.merchant.exception;

import com.ewallet.merchant.entity.Currency;
import java.util.UUID;

public class MerchantWalletNotFoundException extends RuntimeException {
    public MerchantWalletNotFoundException(UUID merchantId, Currency currency) {
        super("Merchant wallet not found for merchant id: " + merchantId + " and currency: " + currency);
    }
}
