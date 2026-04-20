package com.abetappteam.abetapp.filter;

import com.abetappteam.abetapp.config.RateLimitingConfig;
import com.abetappteam.abetapp.config.RateLimitProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Rate limiting filter that applies Bucket4j-based rate limiting to incoming API requests.
 * Restricts the number of requests per IP address within a specified time window.
 * Can be disabled via app.rate-limit.enabled property.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired(required = false)
    private RateLimitingConfig rateLimitingConfig;

    @Autowired(required = false)
    private RateLimitProperties rateLimitProperties;

    // Paths that should not be rate limited
    private static final String[] RATE_LIMIT_EXCLUDED_PATHS = {
            "/",
            "/index.html",
            "/assets/",
            "/css/",
            "/js/",
            "/favicon.ico",
            "/h2-console/",
            "/api/users/login",
            "/api/users/signup"
    };

    private static final String RATE_LIMIT_HEADER = "X-Rate-Limit-Remaining";
    private static final String RETRY_AFTER_HEADER = "Retry-After";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // If rate limiting dependencies are not available, skip rate limiting
        if (rateLimitingConfig == null || rateLimitProperties == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if rate limiting is enabled
        if (!rateLimitProperties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if this request should be rate limited
        if (shouldApplyRateLimit(request)) {
            String clientIp = getClientIp(request);
            String endpoint = request.getRequestURI();

            // Use strict rate limiting for sensitive endpoints
            Bucket bucket = isSensitiveEndpoint(endpoint) ?
                    rateLimitingConfig.resolveStrictBucket(clientIp) :
                    rateLimitingConfig.resolveBucket(clientIp);

            // Try to consume a token
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                // Token consumed successfully, add rate limit info to response
                response.addHeader(RATE_LIMIT_HEADER, String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                long waitForRefill = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());
                logger.warn("Rate limit exceeded for IP: {}, endpoint: {}, waiting: {} seconds",
                        clientIp, endpoint, waitForRefill);

                // Send 429 Too Many Requests response
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.addHeader(RETRY_AFTER_HEADER, String.valueOf(waitForRefill));
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Rate limit exceeded. Please try again in " + waitForRefill + " seconds.\"}"
                );
            }
        } else {
            // No rate limiting for this request
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Determine if rate limiting should be applied to this request.
     * Returns false for excluded paths.
     */
    private boolean shouldApplyRateLimit(HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        for (String excludedPath : RATE_LIMIT_EXCLUDED_PATHS) {
            if (requestUri.equals(excludedPath) || requestUri.startsWith(excludedPath)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determine if an endpoint is sensitive and should use stricter rate limiting.
     */
    private boolean isSensitiveEndpoint(String endpoint) {
        return endpoint.contains("/login") ||
                endpoint.contains("/signup") ||
                endpoint.contains("/password") ||
                endpoint.contains("/admin/");
    }

    /**
     * Extract the client's IP address from the request.
     * Accounts for proxies and load balancers.
     */
    private String getClientIp(HttpServletRequest request) {
        // Check for X-Forwarded-For header (used by proxies)
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, get the first one
            return forwardedFor.split(",")[0].trim();
        }

        // Check for X-Real-IP header
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }

        // Fall back to remote address
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // We handle exclusions in shouldApplyRateLimit method instead
        return false;
    }
}
