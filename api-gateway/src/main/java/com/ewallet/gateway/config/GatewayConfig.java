package com.ewallet.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Wallet Service Routes
                .route("wallet-service", r -> r
                        .path("/api/wallet/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("walletServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/wallet"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY)))
                        .uri("lb://wallet-service"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("paymentServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/payment"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY)))
                        .uri("lb://payment-service"))

                // Notification Service Routes
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("notificationServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/notification"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(2)
                                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY)))
                        .uri("lb://notification-service"))

                // Merchant Service Routes
                .route("merchant-service", r -> r
                        .path("/api/merchants/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("merchantServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/merchant"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY)))
                        .uri("lb://merchant-service"))

                // Ledger Service Routes
                .route("ledger-service", r -> r
                        .path("/api/ledger/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(config -> config
                                        .setName("ledgerServiceCircuitBreaker")
                                        .setFallbackUri("forward:/fallback/ledger"))
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                                                org.springframework.http.HttpStatus.BAD_GATEWAY)))
                        .uri("lb://ledger-service"))

                .build();
    }
}
