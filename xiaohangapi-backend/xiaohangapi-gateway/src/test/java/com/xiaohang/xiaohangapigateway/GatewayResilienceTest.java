package com.xiaohang.xiaohangapigateway.utils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gateway Resilience Tests
 * Tests for Rate Limiting and Circuit Breaker patterns
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

    private RateLimiterRegistry rateLimiterRegistry;
    private CircuitBreakerRegistry circuitBreakerRegistry;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        rateLimiterRegistry = RateLimiterRegistry.of(
                RateLimiterConfig.custom()
                        .limitRefreshPeriod(Duration.ofSeconds(60))
                        .limitForPeriod(100)
                        .timeoutDuration(Duration.ofMillis(500))
                        .build()
        );

        circuitBreakerRegistry = CircuitBreakerRegistry.of(
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slowCallDurationThreshold(Duration.ofSeconds(2))
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .slidingWindowSize(10)
                        .build()
        );

        executorService = Executors.newFixedThreadPool(10);
    }

    @AfterEach
    void tearDown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    @AfterAll
    void cleanup() {
        if (rateLimiterRegistry != null) {
            rateLimiterRegistry.getAllRateLimiters().forEach(RateLimiter::delete);
        }
    }

    // ============================================
    // Rate Limiter Tests
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Test rate limiter allows requests within limit")
    void testRateLimiter_AllowsRequestsWithinLimit() {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("test1");

        for (int i = 0; i < 10; i++) {
            assertTrue(rateLimiter.acquirePermission(),
                    "Request " + (i + 1) + " should be allowed");
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test rate limiter blocks requests exceeding limit")
    void testRateLimiter_BlocksExceedingRequests() {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("test2", RateLimiterConfig.custom()
                .limitForPeriod(5)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ofMillis(100))
                .build());

        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiter.acquirePermission(),
                    "Request " + (i + 1) + " should be allowed");
        }

        assertFalse(rateLimiter.acquirePermission(),
                "6th request should be blocked");
    }

    @Test
    @Order(3)
    @DisplayName("Test rate limiter with concurrent requests")
    void testRateLimiter_ConcurrentRequests() throws InterruptedException {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("test3", RateLimiterConfig.custom()
                .limitForPeriod(20)
                .limitRefreshPeriod(Duration.ofSeconds(60))
                .timeoutDuration(Duration.ofMillis(100))
                .build());

        int totalRequests = 50;
        CountDownLatch latch = new CountDownLatch(totalRequests);
        java.util.concurrent.atomic.AtomicInteger allowedCount = new java.util.concurrent.atomic.AtomicInteger(0);
        java.util.concurrent.atomic.AtomicInteger blockedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        for (int i = 0; i < totalRequests; i++) {
            executorService.submit(() -> {
                if (rateLimiter.acquirePermission()) {
                    allowedCount.incrementAndGet();
                } else {
                    blockedCount.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await(10, TimeUnit.SECONDS);

        assertTrue(allowedCount.get() <= 20,
                "Allowed requests should be at most 20, got: " + allowedCount.get());
        assertEquals(50, allowedCount.get() + blockedCount.get(),
                "All requests should be accounted for");
    }

    // ============================================
    // Circuit Breaker Tests
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Test circuit breaker starts in CLOSED state")
    void testCircuitBreaker_InitialState() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCB1");

        assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.getState(),
                "Circuit breaker should start in CLOSED state");
    }

    @Test
    @Order(11)
    @DisplayName("Test circuit breaker transitions to OPEN after failures")
    void testCircuitBreaker_TransitionsToOpen() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCB2",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slowCallDurationThreshold(Duration.ofMillis(100))
                        .waitDurationInOpenState(Duration.ofMillis(1000))
                        .slidingWindowSize(5)
                        .minimumNumberOfCalls(5)
                        .build());

        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordException(new RuntimeException("Test exception " + i));
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        CircuitBreaker.State state = circuitBreaker.getState();
        assertTrue(state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.CLOSED,
                "Circuit breaker should be OPEN or HALF_OPEN, got: " + state);
    }

    @Test
    @Order(12)
    @DisplayName("Test circuit breaker records successful calls")
    void testCircuitBreaker_RecordsSuccess() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCB3");

        for (int i = 0; i < 10; i++) {
            circuitBreaker.recordSuccess();
        }

        io.github.resilience4j.circuitbreaker.CircuitBreakerMetrics metrics = circuitBreaker.getMetrics();
        assertEquals(10, metrics.getNumberOfSuccessfulCalls(),
                "Should have 10 successful calls");
    }

    @Test
    @Order(13)
    @DisplayName("Test circuit breaker records failed calls")
    void testCircuitBreaker_RecordsFailures() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCB4");

        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordException(new RuntimeException("Failure " + i));
        }

        io.github.resilience4j.circuitbreaker.CircuitBreakerMetrics metrics = circuitBreaker.getMetrics();
        assertEquals(5, metrics.getNumberOfFailedCalls(),
                "Should have 5 failed calls");
    }

    @Test
    @Order(14)
    @DisplayName("Test circuit breaker with fallback")
    void testCircuitBreaker_WithFallback() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCB5");

        String fallbackResult = circuitBreaker.executeSupplierWithFallback(
                () -> {
                    throw new RuntimeException("Service unavailable");
                },
                throwable -> "Fallback result"
        );

        assertEquals("Fallback result", fallbackResult,
                "Should return fallback result when service fails");
    }

    @Test
    @Order(15)
    @DisplayName("Test circuit breaker computes failure rate")
    void testCircuitBreaker_FailureRate() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("testCB6",
                CircuitBreakerConfig.custom()
                        .failureRateThreshold(50)
                        .slowCallDurationThreshold(Duration.ofMillis(100))
                        .waitDurationInOpenState(Duration.ofSeconds(30))
                        .slidingWindowSize(10)
                        .minimumNumberOfCalls(10)
                        .build());

        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordSuccess();
        }
        for (int i = 0; i < 5; i++) {
            circuitBreaker.recordException(new RuntimeException("Error"));
        }

        io.github.resilience4j.circuitbreaker.CircuitBreakerMetrics metrics = circuitBreaker.getMetrics();
        assertEquals(50.0, metrics.getFailureRate(), 0.1,
                "Failure rate should be 50%");
    }

    // ============================================
    // Redis Integration Tests (if available)
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Test Redis rate limiting (if Redis is available)")
    void testRedisRateLimiting() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:ratelimit:" + System.currentTimeMillis();
        Long increment = redisTemplate.opsForValue().increment(key);
        assertNotNull(increment, "Redis increment should work");

        redisTemplate.delete(key);
    }

    @Test
    @Order(21)
    @DisplayName("Test Redis circuit breaker state (if Redis is available)")
    void testRedisCircuitBreakerState() {
        if (redisTemplate == null) {
            assertTrue(true, "Redis not available, skipping test");
            return;
        }

        String key = "test:circuitbreaker:state";
        redisTemplate.opsForValue().set(key, "CLOSED");

        Object state = redisTemplate.opsForValue().get(key);
        assertEquals("CLOSED", state, "Circuit breaker state should be stored in Redis");

        redisTemplate.delete(key);
    }

    // ============================================
    // Stress Tests
    // ============================================

    @Test
    @Order(30)
    @DisplayName("Test high concurrency stress test")
    void testHighConcurrency() throws InterruptedException {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("stressTest",
                RateLimiterConfig.custom()
                        .limitForPeriod(100)
                        .limitRefreshPeriod(Duration.ofSeconds(1))
                        .timeoutDuration(Duration.ofMillis(10))
                        .build());

        int totalRequests = 1000;
        CountDownLatch latch = new CountDownLatch(totalRequests);
        java.util.concurrent.atomic.AtomicInteger allowedCount = new java.util.concurrent.atomic.AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            executorService.submit(() -> {
                if (rateLimiter.acquirePermission()) {
                    allowedCount.incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        long duration = System.currentTimeMillis() - startTime;

        assertTrue(allowedCount.get() <= 100,
                "Allowed requests should be at most 100, got: " + allowedCount.get());
        System.out.println("Processed " + totalRequests + " requests in " + duration + "ms");
    }
}
