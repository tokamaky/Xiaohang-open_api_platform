package com.xiaohang.xiaohangapigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Circuit breaker configuration properties.
 * Bound from application.yml circuit-breaker.* section.
 */
@Data
@Component
@ConfigurationProperties(prefix = "circuit-breaker")
public class CircuitBreakerConfig {

    private long slowCallDurationMs = 2000;

    private double slowCallRatioThreshold = 0.5;

    private double errorRatioThreshold = 0.5;

    private int minRequestAmount = 5;

    private int waitDurationInOpenStateSeconds = 30;
}
