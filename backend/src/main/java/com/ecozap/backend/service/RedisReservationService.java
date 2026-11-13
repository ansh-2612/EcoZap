package com.ecozap.backend.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisReservationService {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisReservationService(
            RedisTemplate<String, String> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public boolean reserveSlot(
            String slotId,
            String bookingId,
            long durationMinutes) {

        String key = "slot:reservation:" + slotId;

        Boolean reserved =
                redisTemplate.opsForValue()
                        .setIfAbsent(
                                key,
                                bookingId,
                                durationMinutes,
                                TimeUnit.MINUTES
                        );

        return Boolean.TRUE.equals(reserved);
    }

    public void releaseSlot(String slotId) {

        String key = "slot:reservation:" + slotId;

        redisTemplate.delete(key);
    }

    public boolean isSlotReserved(String slotId) {

        String key = "slot:reservation:" + slotId;

        return Boolean.TRUE.equals(
                redisTemplate.hasKey(key)
        );
    }

    public String getReservation(String slotId) {

        String key = "slot:reservation:" + slotId;

        return redisTemplate.opsForValue().get(key);
    }
}