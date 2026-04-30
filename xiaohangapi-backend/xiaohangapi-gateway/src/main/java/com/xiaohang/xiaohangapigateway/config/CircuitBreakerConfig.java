package com.xiaohang.xiaohangapigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Circuit breaker configuration properties.
 * Maps to resilience4j.circuitbreaker.* in application.yml.
 * These values are used to configure the Resilience4j circuit breaker.
 */
@Data
@Component
@ConfigurationProperties(prefix = "resilience4j.circuitbreaker")
public class CircuitBreakerConfig {

    private BackendInterface backendInterface = new BackendInterface();

    @Data
    public static class BackendInterface {
        private int failureRateThreshold = 50;
        private int slowCallRateThreshold = 50;
        private int slowCallDurationMs = 2000;
        private int minimumNumberOfCalls = 5;
        private int waitDurationInOpenStateSeconds = 30;
        private int permittedNumberOfCallsInHalfOpenState = 3;
        private int slidingWindowSize = 10;
        private String slidingWindowType = "COUNT_BASED";
    }
}
