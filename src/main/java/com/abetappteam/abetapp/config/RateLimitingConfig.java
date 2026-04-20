package com.abetappteam.abetapp.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for API rate limiting using Bucket4j.
 * Provides rate limiting buckets for different types of requests.
 */
@Configuration
public class RateLimitingConfig {

    @Autowired
    private RateLimitProperties rateLimitProperties;

    // Store buckets per IP address
    private final Map<String, Bucket> cacheBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> strictCacheBuckets = new ConcurrentHashMap<>();

    /**
     * Get or create a bucket for a specific IP address.
     * Uses token bucket algorithm to limit requests.
     *
     * @param key The identifier (usually IP address)
     * @return A bucket with configured rate limits
     */
    public Bucket resolveBucket(String key) {
        return cacheBuckets.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Create a new rate limiting bucket.
     * Configuration is read from application properties.
     * Default: 100 requests per 1 minute per IP address
     *
     * @return A new configured bucket
     */
    private Bucket createNewBucket() {
        long capacity = rateLimitProperties.getCapacity();
        int refillMinutes = rateLimitProperties.getRefillMinutes();
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create a bucket with custom rate limit configuration.
     * Useful for different endpoints with different limits.
     *
     * @param capacity Maximum number of tokens
     * @param refillMinutes Time window in minutes for refill
     * @return A new configured bucket
     */
    public Bucket createCustomBucket(long capacity, int refillMinutes) {
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create a strict bucket for sensitive operations (login, etc.).
     * Configuration is read from application properties.
     * Default: 10 requests per 15 minutes per IP address
     *
     * @return A new configured bucket with stricter limits
     */
    private Bucket createStrictBucket() {
        long capacity = rateLimitProperties.getStrictCapacity();
        int refillMinutes = rateLimitProperties.getStrictRefillMinutes();
        Bandwidth limit = Bandwidth.classic(capacity, Refill.intervally(capacity, Duration.ofMinutes(refillMinutes)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Get or create a strict bucket for sensitive operations.
     *
     * @param key The identifier (usually IP address)
     * @return A bucket with stricter rate limits
     */
    public Bucket resolveStrictBucket(String key) {
        return strictCacheBuckets.computeIfAbsent(key, k -> createStrictBucket());
    }
}
