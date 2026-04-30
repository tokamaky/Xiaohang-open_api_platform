package com.xiaohang.xiaohangapigateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate limit configuration properties.
 * Bound from application.yml rate-limit.* section.
 */
@Data
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    private LimitRule user = new LimitRule();
    private LimitRule interfaceRule = new LimitRule();
    private LimitRule global = new LimitRule();

    @Data
    public static class LimitRule {
        private int maxRequests = 100;
        private int windowSeconds = 60;
    }
}
