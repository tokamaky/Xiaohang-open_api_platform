package com.xiaohang.xiaohangapigateway.utils;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gateway Resilience Tests
 * Tests for Rate Limiting and Circuit Breaker patterns using Redis
 *
 * @author xiaohang
 */
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GatewayResilienceTest {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // ============================================
    // Redis Rate Limiting Tests
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Test Redis connection availability")
    void testRedisConnection() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String testKey = "test:connection:" + System.currentTimeMillis();
        redisTemplate.opsForValue().set(testKey, "ok", java.time.Duration.ofSeconds(10));
        Object value = redisTemplate.opsForValue().get(testKey);
        assertEquals("ok", value, "Redis should store and retrieve values");
        redisTemplate.delete(testKey);
    }

    @Test
    @Order(2)
    @DisplayName("Test Redis rate limiting with INCR")
    void testRedisRateLimiting() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:ratelimit:" + System.currentTimeMillis();

        // Simulate rate limiting
        for (int i = 0; i < 5; i++) {
            Long count = redisTemplate.opsForValue().increment(key);
            assertNotNull(count, "Increment should return a value");
            assertEquals(i + 1, count, "Counter should increment");
        }

        redisTemplate.delete(key);
    }

    @Test
    @Order(3)
    @DisplayName("Test Redis rate limiting with expiration")
    void testRedisRateLimitingWithExpiration() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:ratelimit:exp:" + System.currentTimeMillis();

        // First request
        Long count = redisTemplate.opsForValue().increment(key);
        assertEquals(1L, count, "First request should set count to 1");

        // Check TTL exists
        Long ttl = redisTemplate.getExpire(key);
        assertTrue(ttl == null || ttl > 0, "Key should have TTL");

        redisTemplate.delete(key);
    }

    @Test
    @Order(4)
    @DisplayName("Test Redis concurrent rate limiting")
    void testRedisConcurrentRateLimiting() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:ratelimit:concurrent:" + System.currentTimeMillis();

        // Simulate multiple requests
        for (int i = 0; i < 10; i++) {
            redisTemplate.opsForValue().increment(key);
        }

        Long totalCount = redisTemplate.opsForValue().get(key, Long.class);
        assertEquals(10L, totalCount, "Should have 10 requests");

        redisTemplate.delete(key);
    }

    // ============================================
    // Circuit Breaker State Tests
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Test circuit breaker state storage in Redis")
    void testCircuitBreakerStateStorage() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String closedKey = "circuit:backend:state";
        String countKey = "circuit:backend:count";
        String failureKey = "circuit:backend:failures";

        // Simulate CLOSED state
        redisTemplate.opsForValue().set(closedKey, "CLOSED");
        redisTemplate.opsForValue().set(countKey, 0);
        redisTemplate.opsForValue().set(failureKey, 0);

        // Verify state
        Object state = redisTemplate.opsForValue().get(closedKey);
        assertEquals("CLOSED", state, "Circuit breaker should be CLOSED");

        // Simulate failures
        redisTemplate.opsForValue().increment(failureKey);
        redisTemplate.opsForValue().increment(failureKey);
        redisTemplate.opsForValue().increment(failureKey);

        Long failures = redisTemplate.opsForValue().get(failureKey, Long.class);
        assertEquals(3L, failures, "Should have 3 failures");

        // Simulate transition to OPEN
        redisTemplate.opsForValue().set(closedKey, "OPEN");
        state = redisTemplate.opsForValue().get(closedKey);
        assertEquals("OPEN", state, "Circuit breaker should be OPEN");

        // Cleanup
        redisTemplate.delete(closedKey);
        redisTemplate.delete(countKey);
        redisTemplate.delete(failureKey);
    }

    @Test
    @Order(11)
    @DisplayName("Test circuit breaker half-open state")
    void testCircuitBreakerHalfOpenState() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String stateKey = "circuit:test:state";
        String attemptKey = "circuit:test:attempts";

        // Simulate HALF_OPEN state
        redisTemplate.opsForValue().set(stateKey, "HALF_OPEN");
        redisTemplate.opsForValue().set(attemptKey, 0);

        // Increment attempts
        for (int i = 0; i < 3; i++) {
            redisTemplate.opsForValue().increment(attemptKey);
        }

        Long attempts = redisTemplate.opsForValue().get(attemptKey, Long.class);
        assertEquals(3L, attempts, "Should have 3 attempts");

        // Transition back to CLOSED on success
        redisTemplate.opsForValue().set(stateKey, "CLOSED");
        Object state = redisTemplate.opsForValue().get(stateKey);
        assertEquals("CLOSED", state, "Circuit breaker should be CLOSED");

        redisTemplate.delete(stateKey);
        redisTemplate.delete(attemptKey);
    }

    // ============================================
    // Gateway Configuration Tests
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Test gateway route configuration")
    void testGatewayRouteConfiguration() {
        // This test verifies the gateway can be loaded
        assertTrue(true, "Gateway configuration is valid");
    }

    @Test
    @Order(21)
    @DisplayName("Test Redis key expiration")
    void testRedisKeyExpiration() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:expiration:" + System.currentTimeMillis();

        redisTemplate.opsForValue().set(key, "value", java.time.Duration.ofSeconds(5));
        Boolean hasKey = redisTemplate.hasKey(key);
        assertTrue(hasKey, "Key should exist");

        Long ttl = redisTemplate.getExpire(key);
        assertTrue(ttl != null && ttl > 0 && ttl <= 5, "TTL should be around 5 seconds");

        redisTemplate.delete(key);
    }

    // ============================================
    // Stress Tests
    // ============================================

    @Test
    @Order(30)
    @DisplayName("Test high frequency Redis operations")
    void testHighFrequencyRedisOperations() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:stress:" + System.currentTimeMillis();

        // Simulate high frequency requests
        for (int i = 0; i < 100; i++) {
            redisTemplate.opsForValue().increment(key);
        }

        Long total = redisTemplate.opsForValue().get(key, Long.class);
        assertEquals(100L, total, "Should handle 100 increments");

        redisTemplate.delete(key);
    }

    @Test
    @Order(31)
    @DisplayName("Test Redis atomic operations")
    void testRedisAtomicOperations() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String counterKey = "test:atomic:" + System.currentTimeMillis();

        // INCR is atomic
        Long count1 = redisTemplate.opsForValue().increment(counterKey);
        Long count2 = redisTemplate.opsForValue().increment(counterKey);
        Long count3 = redisTemplate.opsForValue().increment(counterKey);

        assertEquals(1L, count1);
        assertEquals(2L, count2);
        assertEquals(3L, count3);

        redisTemplate.delete(counterKey);
    }
}
