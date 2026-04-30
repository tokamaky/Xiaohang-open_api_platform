package com.xiaohang.xiaohangapigateway.utils;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resilience4j-based circuit breaker for protecting backend RPC calls.
 *
 * Three circuit breaker states:
 * CLOSED  - Normal operation, requests pass through
 * OPEN    - Circuit is tripped, requests are rejected immediately
 * HALF_OPEN - After waitDurationInOpenState, allows limited requests to test recovery
 *
 * This replaces the previous Alibaba Sentinel implementation for better compatibility
 * with the Spring Cloud ecosystem and North American job market requirements.
 */
@Slf4j
@Component
public class Resilience4jCircuitBreaker {

    private static final String BACKEND_CB = "backendInterface";

    private final CircuitBreakerRegistry registry;
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public Resilience4jCircuitBreaker() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slowCallRateThreshold(50)
                .slowCallDurationThreshold(Duration.ofMillis(2000))
                .minimumNumberOfCalls(5)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .build();
        this.registry = CircuitBreakerRegistry.of(defaultConfig);
    }

    @PostConstruct
    public void init() {
        CircuitBreaker cb = registry.circuitBreaker(BACKEND_CB);

        cb.getEventPublisher()
                .onStateTransition(event -> log.info("Circuit breaker [{}] state transition: {} -> {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()))
                .onFailureRateExceeded(event -> log.warn("Circuit breaker [{}] failure rate exceeded: {}%",
                        event.getCircuitBreakerName(),
                        event.getFailureRate()))
                .onSlowCallRateExceeded(event -> log.warn("Circuit breaker [{}] slow call rate exceeded: {}%",
                        event.getCircuitBreakerName(),
                        event.getSlowCallRate()));

        circuitBreakers.put(BACKEND_CB, cb);
        log.info("Resilience4j circuit breaker initialized for resource: {}", BACKEND_CB);
    }

    /**
     * Execute a supplier with circuit breaker protection.
     *
     * @param supplier the callable to execute
     * @param <T>     return type
     * @return result from the supplier
     * @throws CircuitBreakerOpenException if the circuit is open
     */
    public <T> T execute(java.util.function.Supplier<T> supplier) throws CircuitBreakerOpenException {
        CircuitBreaker cb = circuitBreakers.get(BACKEND_CB);
        if (cb == null) {
            return supplier.get();
        }
        try {
            return CircuitBreaker.decorateSupplier(cb, supplier).get();
        } catch (io.github.resilience4j.circuitbreaker.CallNotPermittedException e) {
            throw new CircuitBreakerOpenException(BACKEND_CB, cb.getState());
        }
    }

    /**
     * Execute a supplier with circuit breaker protection (void return).
     */
    public void executeVoid(java.util.function.Supplier<Void> supplier) {
        CircuitBreaker cb = circuitBreakers.get(BACKEND_CB);
        if (cb == null) {
            supplier.get();
            return;
        }
        CircuitBreaker.decorateRunnable(cb, supplier::get).run();
    }

    /**
     * Check if the circuit breaker is currently open.
     */
    public boolean isCircuitOpen() {
        CircuitBreaker cb = circuitBreakers.get(BACKEND_CB);
        if (cb == null) {
            return false;
        }
        return cb.getState() == CircuitBreaker.State.OPEN;
    }

    /**
     * Check if the circuit breaker is currently in half-open state.
     */
    public boolean isCircuitHalfOpen() {
        CircuitBreaker cb = circuitBreakers.get(BACKEND_CB);
        if (cb == null) {
            return false;
        }
        return cb.getState() == CircuitBreaker.State.HALF_OPEN;
    }

    /**
     * Get the current state of the circuit breaker as a string.
     */
    public String getCircuitState() {
        CircuitBreaker cb = circuitBreakers.get(BACKEND_CB);
        if (cb == null) {
            return "NOT_INITIALIZED";
        }
        return cb.getState().toString();
    }

    /**
     * Get the circuit breaker metrics (failure rate, slow call rate, etc.).
     */
    public CircuitBreakerMetrics getMetrics() {
        CircuitBreaker cb = circuitBreakers.get(BACKEND_CB);
        if (cb == null) {
            return new CircuitBreakerMetrics(-1, -1, -1, "NOT_INITIALIZED");
        }
        CircuitBreaker.Metrics metrics = cb.getMetrics();
        return new CircuitBreakerMetrics(
                metrics.getFailureRate(),
                metrics.getSlowCallRate(),
                metrics.getNumberOfSuccessfulCalls(),
                cb.getState().toString()
        );
    }

    public record CircuitBreakerMetrics(
            double failureRate,
            double slowCallRate,
            long successfulCalls,
            String state
    ) {}

    /**
     * Custom exception thrown when circuit breaker is open.
     * Extends Exception (checked) so it can be caught independently from RuntimeException.
     */
    public static class CircuitBreakerOpenException extends Exception {
        private final String circuitBreakerName;
        private final CircuitBreaker.State state;

        public CircuitBreakerOpenException(String circuitBreakerName, CircuitBreaker.State state) {
            super("Circuit breaker [" + circuitBreakerName + "] is " + state);
            this.circuitBreakerName = circuitBreakerName;
            this.state = state;
        }

        public String getCircuitBreakerName() {
            return circuitBreakerName;
        }

        public CircuitBreaker.State getState() {
            return state;
        }
    }
}
