package com.ewallet.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();

        log.info("Incoming request: {} {} from {}",
                request.getMethod(),
                request.getPath(),
                request.getRemoteAddress());

        log.debug("Request headers: {}", request.getHeaders());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            Duration duration = Duration.between(startTime, Instant.now());

            log.info("Outgoing response: {} {} - Status: {} - Duration: {}ms",
                    request.getMethod(),
                    request.getPath(),
                    response.getStatusCode(),
                    duration.toMillis());

            log.debug("Response headers: {}", response.getHeaders());
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
