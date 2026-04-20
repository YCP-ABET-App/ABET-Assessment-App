package com.abetappteam.abetapp;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.abetappteam.abetapp.config.RateLimitingConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.capacity=5",
        "app.rate-limit.refill-minutes=1",
        "app.rate-limit.strict-capacity=2",
        "app.rate-limit.strict-refill-minutes=1"
})
public class RateLimitingIntegrationTest {
// TODO: Revisit after milestone 3
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private RateLimitingConfig rateLimitingConfig;
//
//    /**
//     * Test that bucket4j token bucket algorithm works correctly
//     */
//    @Test
//    public void testBucketTokenConsumption() {
//        Bucket bucket = rateLimitingConfig.resolveBucket("test-ip-1");
//
//        // Test consuming tokens
//        for (int i = 0; i < 5; i++) {
//            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
//            assert probe.isConsumed();
//        }
//
//        // Next token consumption should fail
//        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
//        assert !probe.isConsumed();
//    }
//
//    /**
//     * Test that strict buckets have lower capacity
//     */
//    @Test
//    public void testStrictBucketLowerCapacity() {
//        String testIp = "test-ip-strict";
//        Bucket strictBucket = rateLimitingConfig.resolveStrictBucket(testIp);
//
//        // Consume tokens up to limit
//        ConsumptionProbe probe1 = strictBucket.tryConsumeAndReturnRemaining(1);
//        assert probe1.isConsumed();
//
//        ConsumptionProbe probe2 = strictBucket.tryConsumeAndReturnRemaining(1);
//        assert probe2.isConsumed();
//
//        // Third request should fail (strict limit is 2)
//        ConsumptionProbe probe3 = strictBucket.tryConsumeAndReturnRemaining(1);
//        assert !probe3.isConsumed();
//    }
//
//    /**
//     * Test that different IPs have separate buckets
//     */
//    @Test
//    public void testPerIPBuckets() {
//        String ip1 = "192.168.1.1";
//        String ip2 = "192.168.1.2";
//
//        Bucket bucket1 = rateLimitingConfig.resolveBucket(ip1);
//        Bucket bucket2 = rateLimitingConfig.resolveBucket(ip2);
//
//        // Consume tokens from bucket1
//        for (int i = 0; i < 5; i++) {
//            bucket1.tryConsumeAndReturnRemaining(1);
//        }
//
//        // Bucket2 should still have tokens
//        ConsumptionProbe probe = bucket2.tryConsumeAndReturnRemaining(1);
//        assert probe.isConsumed();
//    }
//
//    /**
//     * Test custom bucket creation
//     */
//    @Test
//    public void testCustomBucketCreation() {
//        // Create bucket with 20 requests per 5 minutes
//        Bucket customBucket = rateLimitingConfig.createCustomBucket(20, 5);
//
//        // Should be able to consume up to capacity
//        for (int i = 0; i < 20; i++) {
//            ConsumptionProbe probe = customBucket.tryConsumeAndReturnRemaining(1);
//            assert probe.isConsumed();
//        }
//
//        // Next request should fail
//        ConsumptionProbe probe = customBucket.tryConsumeAndReturnRemaining(1);
//        assert !probe.isConsumed();
//    }

}
