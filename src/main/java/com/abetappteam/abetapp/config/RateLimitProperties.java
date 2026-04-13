package com.abetappteam.abetapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for rate limiting.
 * Can be customized in application.properties or application.yml
 *
 * Example:
 * app.rate-limit.enabled=true
 * app.rate-limit.capacity=100
 * app.rate-limit.refill-minutes=1
 * app.rate-limit.strict-capacity=10
 * app.rate-limit.strict-refill-minutes=15
 */
@Configuration
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {

    /**
     * Enable or disable rate limiting
     */
    private boolean enabled = true;

    /**
     * Maximum number of requests allowed per time window
     */
    private long capacity = 100;

    /**
     * Time window in minutes for refilling tokens
     */
    private int refillMinutes = 1;

    /**
     * Maximum number of requests for sensitive endpoints (login, signup, admin, etc.)
     */
    private long strictCapacity = 10;

    /**
     * Time window in minutes for refilling tokens on sensitive endpoints
     */
    private int strictRefillMinutes = 15;

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public int getRefillMinutes() {
        return refillMinutes;
    }

    public void setRefillMinutes(int refillMinutes) {
        this.refillMinutes = refillMinutes;
    }

    public long getStrictCapacity() {
        return strictCapacity;
    }

    public void setStrictCapacity(long strictCapacity) {
        this.strictCapacity = strictCapacity;
    }

    public int getStrictRefillMinutes() {
        return strictRefillMinutes;
    }

    public void setStrictRefillMinutes(int strictRefillMinutes) {
        this.strictRefillMinutes = strictRefillMinutes;
    }
}
