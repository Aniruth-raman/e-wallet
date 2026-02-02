package com.ewallet.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Order(-2)
@Configuration
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status = determineHttpStatus(ex);
        response.setStatusCode(status);

        Map<String, Object> errorAttributes = createErrorAttributes(exchange, ex, status);

        try {
            String errorJson = objectMapper.writeValueAsString(errorAttributes);
            DataBuffer buffer = response.bufferFactory()
                    .wrap(errorJson.getBytes(StandardCharsets.UTF_8));
            
            log.error("Error processing request: {} - {}", 
                    exchange.getRequest().getPath(), 
                    ex.getMessage(), 
                    ex);

            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error creating error response", e);
            return response.setComplete();
        }
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else if (ex instanceof ResponseStatusException) {
            return HttpStatus.resolve(((ResponseStatusException) ex).getStatusCode().value());
        } else if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof IllegalStateException) {
            return HttpStatus.CONFLICT;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private Map<String, Object> createErrorAttributes(
            ServerWebExchange exchange, 
            Throwable ex, 
            HttpStatus status) {
        
        Map<String, Object> errorAttributes = new HashMap<>();
        errorAttributes.put("timestamp", LocalDateTime.now().toString());
        errorAttributes.put("path", exchange.getRequest().getPath().value());
        errorAttributes.put("status", status.value());
        errorAttributes.put("error", status.getReasonPhrase());
        errorAttributes.put("message", determineErrorMessage(ex));
        errorAttributes.put("requestId", 
                exchange.getRequest().getHeaders().getFirst("X-Request-Id"));

        return errorAttributes;
    }

    private String determineErrorMessage(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return "Service is currently unavailable. Please try again later.";
        } else if (ex.getMessage() != null && !ex.getMessage().isEmpty()) {
            return ex.getMessage();
        }
        return "An unexpected error occurred";
    }
}
