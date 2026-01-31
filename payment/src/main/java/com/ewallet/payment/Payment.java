package com.ewallet.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Main class for Payment Service
 */
@SpringBootApplication
public class Payment {
    public static void main(String[] args) {
        SpringApplication.run(Payment.class, args);
    }

    // added RestTemplate bean for HTTP calls to wallet service
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
