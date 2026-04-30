package com.xiaohang.xiaohangapigateway.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * Distributed rate limiter using Redis + Lua sliding window algorithm.
 *
 * Three dimensions of rate limiting:
 * 1. Per-user (by accessKey): limits how many requests each user can make per minute
 * 2. Per-interface (by path): limits how many requests each API can receive per minute
 * 3. Global: limits total requests across all users per minute
 *
 * The sliding window algorithm uses Redis sorted sets (ZSET) with timestamps as scores.
 * This provides more accurate rate limiting than fixed-window counters while
 * remaining memory-efficient.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimiter {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Lua script for sliding window rate limiting.
     *
     * KEYS[1] = Redis key (e.g., "rate_limit:user:{accessKey}")
     * ARGV[1] = current timestamp (milliseconds)
     * ARGV[2] = window size (milliseconds)
     * ARGV[3] = max requests allowed within the window
     *
     * Returns: 1 if allowed, 0 if rate limited
     *
     * Algorithm:
     * 1. Remove all entries older than the window boundary (ZREMRANGEBYSCORE)
     * 2. Count current entries in window (ZCARD)
     * 3. If count < limit, add new entry and return 1 (allowed)
     * 4. Otherwise return 0 (denied)
     *
     * All operations are atomic due to Lua script execution.
     */
    private static final String SLIDING_WINDOW_LUA = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])

        -- Calculate the window start boundary
        local window_start = now - window

        -- Remove all entries outside the sliding window
        redis.call('ZREMRANGEBYSCORE', key, '-inf', window_start)

        -- Count requests currently in the window
        local current = redis.call('ZCARD', key)

        if current < limit then
            -- Add the current request with timestamp as score and a unique member
            -- Using timestamp + random to handle concurrent requests at same ms
            redis.call('ZADD', key, now, now .. ':' .. math.random())
            -- Set key expiration to clean up automatically after window passes
            redis.call('PEXPIRE', key, window + 1000)
            return 1
        else
            return 0
        end
        """;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT;

    static {
        RATE_LIMIT_SCRIPT = new DefaultRedisScript<>();
        RATE_LIMIT_SCRIPT.setScriptText(SLIDING_WINDOW_LUA);
        RATE_LIMIT_SCRIPT.setResultType(Long.class);
    }

    private static final String KEY_PREFIX = "rate_limit:";

    /**
     * Check rate limit for a user (by accessKey).
     *
     * @param accessKey the user's access key
     * @param maxRequests maximum requests allowed in the window
     * @param windowSeconds time window in seconds
     * @return true if the request is allowed, false if rate limited
     */
    public boolean isUserAllowed(String accessKey, int maxRequests, int windowSeconds) {
        String key = KEY_PREFIX + "user:" + accessKey;
        return checkRateLimit(key, maxRequests, windowSeconds);
    }

    /**
     * Check rate limit for an interface (by API path).
     *
     * @param path the API endpoint path
     * @param maxRequests maximum requests allowed in the window
     * @param windowSeconds time window in seconds
     * @return true if the request is allowed, false if rate limited
     */
    public boolean isInterfaceAllowed(String path, int maxRequests, int windowSeconds) {
        String key = KEY_PREFIX + "interface:" + path;
        return checkRateLimit(key, maxRequests, windowSeconds);
    }

    /**
     * Check global rate limit across all requests.
     *
     * @param maxRequests maximum requests allowed in the window
     * @param windowSeconds time window in seconds
     * @return true if the request is allowed, false if rate limited
     */
    public boolean isGlobalAllowed(int maxRequests, int windowSeconds) {
        String key = KEY_PREFIX + "global";
        return checkRateLimit(key, maxRequests, windowSeconds);
    }

    /**
     * Core rate limit check using sliding window algorithm.
     *
     * @param key the Redis key for this rate limit bucket
     * @param maxRequests maximum requests allowed in the window
     * @param windowSeconds time window in seconds
     * @return true if allowed, false if rate limited
     */
    private boolean checkRateLimit(String key, int maxRequests, int windowSeconds) {
        try {
            long now = System.currentTimeMillis();
            long windowMillis = windowSeconds * 1000L;

            Long result = stringRedisTemplate.execute(
                    RATE_LIMIT_SCRIPT,
                    Collections.singletonList(key),
                    String.valueOf(now),
                    String.valueOf(windowMillis),
                    String.valueOf(maxRequests)
            );

            boolean allowed = result != null && result == 1L;
            if (!allowed) {
                log.warn("Rate limit triggered: key={}, maxRequests={}, windowSeconds={}",
                        key, maxRequests, windowSeconds);
            }
            return allowed;
        } catch (Exception e) {
            log.error("Rate limit check failed, allowing request to prevent service disruption: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Get the remaining request quota for a user within the current window.
     *
     * @param accessKey the user's access key
     * @param maxRequests maximum requests allowed in the window
     * @param windowSeconds time window in seconds
     * @return number of remaining requests, or -1 on error
     */
    public long getUserRemainingQuota(String accessKey, int maxRequests, int windowSeconds) {
        String key = KEY_PREFIX + "user:" + accessKey;
        return getRemainingQuota(key, maxRequests, windowSeconds);
    }

    private long getRemainingQuota(String key, int maxRequests, int windowSeconds) {
        try {
            long now = System.currentTimeMillis();
            long windowStart = now - (windowSeconds * 1000L);

            stringRedisTemplate.opsForZSet().removeRangeByScore(key, Double.NEGATIVE_INFINITY, windowStart);
            Long current = stringRedisTemplate.opsForZSet().zCard(key);

            return Math.max(0, maxRequests - (current != null ? current.intValue() : 0));
        } catch (Exception e) {
            log.error("Failed to get remaining quota: {}", e.getMessage());
            return -1;
        }
    }
}
