package com.github.yaliyev.cloudstorageservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Tries to claim an event key in Redis.
     * @param eventKey Unique key (e.g., "event:s3:" + objectKey + ":" + eTag)
     * @param ttl How long to keep the deduplication lock (e.g., 24 hours)
     * @return true if event is NEW and successfully claimed; false if ALREADY PROCESSED.
     */
    public boolean processIfFirstTime(String eventKey, Duration ttl) {
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(eventKey, "PROCESSED", ttl);
        return Boolean.TRUE.equals(isNew);
    }
}
