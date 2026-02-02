package com.ewallet.wallet.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String LOCK_PREFIX = "wallet:lock:";
    private static final Duration LOCK_TIMEOUT = Duration.ofSeconds(30);
    private static final ThreadLocal<String> LOCK_VALUES = new ThreadLocal<>();

    public boolean tryLock(String key, String value) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(LOCK_PREFIX + key, value, LOCK_TIMEOUT);
            if (Boolean.TRUE.equals(result)) {
                LOCK_VALUES.set(value);
                log.debug("Acquired lock for key: {}", key);
            } else {
                log.debug("Failed to acquire lock for key: {}", key);
            }
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Error acquiring lock for key: {}", key, e);
            return false;
        }
    }

    public boolean tryLock(String key) {
        return tryLock(key, UUID.randomUUID().toString());
    }

    public void releaseLock(String key) {
        try {
            String lockValue = LOCK_VALUES.get();
            if (lockValue != null) {
                String storedValue = redisTemplate.opsForValue().get(LOCK_PREFIX + key);
                if (lockValue.equals(storedValue)) {
                    redisTemplate.delete(LOCK_PREFIX + key);
                    log.debug("Released lock for key: {}", key);
                } else {
                    log.warn("Lock ownership mismatch for key: {}. Not releasing.", key);
                }
                LOCK_VALUES.remove();
            }
        } catch (Exception e) {
            log.error("Error releasing lock for key: {}", key, e);
        }
    }
}
