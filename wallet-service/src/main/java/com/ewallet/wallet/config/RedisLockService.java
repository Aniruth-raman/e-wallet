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

    public boolean tryLock(String key, String value) {
        try {
            Boolean result = redisTemplate.opsForValue()
                    .setIfAbsent(LOCK_PREFIX + key, value, LOCK_TIMEOUT);
            log.debug("Attempting to acquire lock for key: {}, result: {}", key, result);
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
            redisTemplate.delete(LOCK_PREFIX + key);
            log.debug("Released lock for key: {}", key);
        } catch (Exception e) {
            log.error("Error releasing lock for key: {}", key, e);
        }
    }
}
