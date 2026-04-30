package com.xiaohang.xiaohangapigateway;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.xiaohang.exception.BusinessException;
import com.xiaohang.xiaohangapiclientsdk.utils.SignUtils;
import cn.hutool.core.util.StrUtil;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.service.InnerInterfaceInfoService;
import com.xiaohang.xiaohangapicommon.service.InnerUserInterfaceInfoService;
import com.xiaohang.xiaohangapicommon.service.InnerUserService;
import com.xiaohang.xiaohangapigateway.config.CircuitBreakerConfig;
import com.xiaohang.xiaohangapigateway.config.RateLimitConfig;
import com.xiaohang.xiaohangapigateway.utils.RateLimiter;
import com.xiaohang.xiaohangapigateway.utils.SentinelCircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference(url = "${DUBBO_BACKEND_URL}")
    private InnerUserService innerUserService;

    @DubboReference(url = "${DUBBO_BACKEND_URL}")
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference(url = "${DUBBO_BACKEND_URL}")
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    private final RateLimiter rateLimiter;
    private final RateLimitConfig rateLimitConfig;
    private final SentinelCircuitBreaker sentinelCircuitBreaker;
    private final CircuitBreakerConfig circuitBreakerConfig;

    public CustomGlobalFilter(RateLimiter rateLimiter, RateLimitConfig rateLimitConfig,
                              SentinelCircuitBreaker sentinelCircuitBreaker, CircuitBreakerConfig circuitBreakerConfig) {
        this.rateLimiter = rateLimiter;
        this.rateLimitConfig = rateLimitConfig;
        this.sentinelCircuitBreaker = sentinelCircuitBreaker;
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    private static final String DYE_DATA_HEADER = "X-Dye-Data";
    private static final String DYE_DATA_VALUE = "nero";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        log.info("Request id: {}", request.getId());
        log.info("Request path: {}", path);
        log.info("Request params: {}", request.getQueryParams());
        log.info("Remote address: {}", request.getRemoteAddress());

        ServerHttpResponse response = exchange.getResponse();

        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String timestamp = headers.getFirst("timestamp");
        String nonce = headers.getFirst("nonce");
        String sign = headers.getFirst("sign");
        String body = URLUtil.decode(headers.getFirst("body"), CharsetUtil.CHARSET_UTF_8);
        String method = headers.getFirst("method");

        log.info("accessKey: {}", accessKey);
        log.info("timestamp: {}", timestamp);
        log.info("nonce: {}", nonce);
        log.info("sign: {}", sign);
        log.info("method: {}", method);

        if (StrUtil.isBlank(nonce)
                || StrUtil.isBlank(sign)
                || StrUtil.isBlank(timestamp)
                || StrUtil.isBlank(method)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Missing required request headers");
        }

        User invokeUser = innerUserService.getInvokeUser(accessKey);
        if (invokeUser == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Invalid accessKey");
        }

        long currentSeconds = System.currentTimeMillis() / 1000;
        long requestSeconds = Long.parseLong(timestamp);
        if (Math.abs(currentSeconds - requestSeconds) > 300) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Request timestamp expired");
        }

        String serverSign = SignUtils.genSign(body, invokeUser.getSecretKey());
        if (!sign.equals(serverSign)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Invalid signature");
        }

        if (!doRateLimit(accessKey, path)) {
            throw new BusinessException(ErrorCode.RATE_LIMIT_ERROR, "Rate limit exceeded, please try again later");
        }

        if (!doCircuitBreakerCheck(path)) {
            throw new BusinessException(ErrorCode.CIRCUIT_BREAKER_ERROR, "Service temporarily unavailable, please try again later");
        }

        InterfaceInfo interfaceInfo;
        try {
            interfaceInfo = queryInterfaceInfoWithCircuitBreaker(path, method);
        } catch (BlockException e) {
            log.error("Circuit breaker triggered while querying interface info for path: {}", path, e);
            throw new BusinessException(ErrorCode.CIRCUIT_BREAKER_ERROR, "Service temporarily unavailable, please try again later");
        } catch (Exception e) {
            log.error("Failed to query interface info", e);
            interfaceInfo = null;
        }
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface not found");
        }

        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());
    }

    /**
     * Execute three-dimensional rate limiting:
     * 1. Global limit - protects the entire platform from traffic spikes
     * 2. Per-user limit - prevents a single user from consuming too many resources
     * 3. Per-interface limit - prevents a single API from being overwhelmed
     */
    private boolean doRateLimit(String accessKey, String path) {
        boolean globalAllowed = rateLimiter.isGlobalAllowed(
                rateLimitConfig.getGlobal().getMaxRequests(),
                rateLimitConfig.getGlobal().getWindowSeconds()
        );
        if (!globalAllowed) {
            log.warn("Global rate limit triggered for path: {}", path);
            return false;
        }

        boolean userAllowed = rateLimiter.isUserAllowed(
                accessKey,
                rateLimitConfig.getUser().getMaxRequests(),
                rateLimitConfig.getUser().getWindowSeconds()
        );
        if (!userAllowed) {
            log.warn("Per-user rate limit triggered for accessKey: {}", accessKey);
            return false;
        }

        boolean interfaceAllowed = rateLimiter.isInterfaceAllowed(
                path,
                rateLimitConfig.getInterfaceRule().getMaxRequests(),
                rateLimitConfig.getInterfaceRule().getWindowSeconds()
        );
        if (!interfaceAllowed) {
            log.warn("Per-interface rate limit triggered for path: {}", path);
            return false;
        }

        return true;
    }

    /**
     * Pre-check circuit breaker before invoking backend.
     * Throws CircuitBreakerOpenException if the circuit is open.
     */
    private boolean doCircuitBreakerCheck(String path) {
        try {
            sentinelCircuitBreaker.execute(() -> {
                return true;
            });
            return true;
        } catch (BlockException e) {
            log.warn("Circuit breaker pre-check triggered for path: {}", path);
            return false;
        }
    }

    /**
     * Query interface info with circuit breaker protection around the Dubbo RPC call.
     */
    private InterfaceInfo queryInterfaceInfoWithCircuitBreaker(String path, String method) throws BlockException {
        return sentinelCircuitBreaker.execute(() -> {
            return innerInterfaceInfoService.getInterfaceInfo(path, method);
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long userId, long interfaceInfoId) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                try {
                                    innerUserInterfaceInfoService.invokeCount(userId, interfaceInfoId);
                                } catch (Exception e) {
                                    log.error("invokeCount error", e);
                                }

                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);

                                String data = new String(content, StandardCharsets.UTF_8);
                                log.info("response body: {}", data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("Unexpected response body type");
                        }
                        return super.writeWith(body);
                    }
                };

                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header(DYE_DATA_HEADER, DYE_DATA_VALUE)
                        .build();

                ServerWebExchange modifiedExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .response(decoratedResponse)
                        .build();
                return chain.filter(modifiedExchange);
            }
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("handleResponse error", e);
            return chain.filter(exchange);
        }
    }
}
