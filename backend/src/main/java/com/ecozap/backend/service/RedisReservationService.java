package com.ecozap.backend.service;

import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
public class RedisReservationService {
    private static final DefaultRedisScript<Long> RELEASE = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end", Long.class);
    private final StringRedisTemplate redis;

    public RedisReservationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public boolean acquire(String slotId, String owner) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(
                "ecozap:booking-lock:" + slotId, owner, Duration.ofSeconds(30)));
    }

    public void release(String slotId, String owner) {
        redis.execute(RELEASE, List.of("ecozap:booking-lock:" + slotId), owner);
    }
}
