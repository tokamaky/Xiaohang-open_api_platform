package com.xiaohang.xiaohangapigateway.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Resilience4jCircuitBreaker Unit Tests")
class Resilience4jCircuitBreakerTest {

    private Resilience4jCircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = new Resilience4jCircuitBreaker();
        circuitBreaker.init();
    }

    @Test
    @DisplayName("Circuit breaker should be CLOSED initially")
    void testInitialState() {
        assertFalse(circuitBreaker.isCircuitOpen());
        assertFalse(circuitBreaker.isCircuitHalfOpen());
        assertEquals("CLOSED", circuitBreaker.getCircuitState());
    }

    @Test
    @DisplayName("Circuit breaker should allow requests in CLOSED state")
    void testAllowRequestsInClosedState() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            String result = circuitBreaker.execute(() -> {
                callCount.incrementAndGet();
                return "success";
            });
            assertEquals("success", result);
        }
        assertEquals(5, callCount.get());
    }

    @Test
    @DisplayName("Circuit breaker should propagate normal exceptions")
    void testNormalExceptionPropagation() {
        RuntimeException expected = assertThrows(RuntimeException.class, () ->
                circuitBreaker.execute(() -> {
                    throw new RuntimeException("Expected error");
                })
        );
        assertEquals("Expected error", expected.getMessage());
    }

    @Test
    @DisplayName("Circuit breaker should allow null return")
    void testNullReturn() throws Exception {
        String result = circuitBreaker.execute(() -> null);
        assertNull(result);
    }

    @Test
    @DisplayName("executeVoid should work without return value")
    void testExecuteVoid() {
        AtomicInteger counter = new AtomicInteger(0);
        assertDoesNotThrow(() -> circuitBreaker.executeVoid(() -> {
            counter.incrementAndGet();
            return null;
        }));
        assertEquals(1, counter.get());
    }

    @Test
    @DisplayName("Circuit breaker metrics should be accessible")
    void testMetrics() {
        Resilience4jCircuitBreaker.CircuitBreakerMetrics metrics = circuitBreaker.getMetrics();
        assertNotNull(metrics);
        assertNotNull(metrics.state());
        assertEquals("CLOSED", metrics.state());
    }

    @Test
    @DisplayName("Circuit breaker should use supplier result types correctly")
    void testSupplierResultTypes() throws Exception {
        Integer intResult = circuitBreaker.execute(() -> 42);
        assertEquals(42, intResult);

        Boolean boolResult = circuitBreaker.execute(() -> true);
        assertTrue(boolResult);

        Long longResult = circuitBreaker.execute(() -> 123L);
        assertEquals(123L, longResult);

        String strResult = circuitBreaker.execute(() -> "hello");
        assertEquals("hello", strResult);
    }

    @Test
    @DisplayName("CircuitBreakerOpenException should contain state information")
    void testCircuitBreakerOpenException() {
        Resilience4jCircuitBreaker.CircuitBreakerOpenException ex =
                new Resilience4jCircuitBreaker.CircuitBreakerOpenException(
                        "testCircuit", io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN);

        assertEquals("testCircuit", ex.getCircuitBreakerName());
        assertEquals(io.github.resilience4j.circuitbreaker.CircuitBreaker.State.OPEN, ex.getState());
        assertTrue(ex.getMessage().contains("testCircuit"));
        assertTrue(ex.getMessage().contains("OPEN"));
    }

    @Test
    @DisplayName("Circuit breaker should throw CircuitBreakerOpenException when tripped")
    void testCircuitBreakerTripped() {
        Resilience4jCircuitBreaker testCb = new Resilience4jCircuitBreaker();
        testCb.init();

        for (int i = 0; i < 20; i++) {
            try {
                testCb.execute(() -> {
                    throw new RuntimeException("Force trip");
                });
            } catch (Exception ignored) {
            }
        }

        assertTrue(
                testCb.isCircuitOpen() || testCb.isCircuitHalfOpen(),
                "Circuit should be OPEN or HALF_OPEN after enough failures"
        );
    }
}
