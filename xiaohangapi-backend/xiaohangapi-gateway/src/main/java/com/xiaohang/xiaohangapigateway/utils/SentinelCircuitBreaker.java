package com.xiaohang.xiaohangapigateway.utils;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.xiaohang.xiaohangapigateway.config.CircuitBreakerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SentinelCircuitBreaker {

    private static final String RESOURCE_NAME = "backend_interface";
    private static final String GATEWAY_CONTEXT = "gateway_context";

    private final CircuitBreakerConfig circuitBreakerConfig;

    public SentinelCircuitBreaker(CircuitBreakerConfig circuitBreakerConfig) {
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    @PostConstruct
    public void init() {
        initDegradeRules();
        log.info("Sentinel circuit breaker initialized with config: slowCallDurationMs={}, slowCallRatioThreshold={}, errorRatioThreshold={}, minRequestAmount={}, waitDurationInOpenStateSeconds={}",
                circuitBreakerConfig.getSlowCallDurationMs(),
                circuitBreakerConfig.getSlowCallRatioThreshold(),
                circuitBreakerConfig.getErrorRatioThreshold(),
                circuitBreakerConfig.getMinRequestAmount(),
                circuitBreakerConfig.getWaitDurationInOpenStateSeconds());
    }

    private void initDegradeRules() {
        DegradeRule rule = new DegradeRule(RESOURCE_NAME)
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                .setCount(0)
                .setSlowRatioThreshold(circuitBreakerConfig.getSlowCallRatioThreshold())
                .setMinRequestAmount(circuitBreakerConfig.getMinRequestAmount())
                .setStatIntervalMs((long) circuitBreakerConfig.getSlowCallDurationMs() * 10)
                .setTimeWindow(circuitBreakerConfig.getWaitDurationInOpenStateSeconds())
                .setSlowRatioThreshold(circuitBreakerConfig.getSlowCallRatioThreshold())
                .setMaxResponseTime(circuitBreakerConfig.getSlowCallDurationMs());

        List<DegradeRule> rules = new ArrayList<>();
        rules.add(rule);
        DegradeRuleManager.loadRules(rules);

        ClusterBuilderSlot.getClusterNodeMap().putIfAbsent(RESOURCE_NAME,
                new com.alibaba.csp.sentinel.slotchain.ClusterBuilderSlot().getClusterNode());
    }

    /**
     * Execute the supplier with circuit breaker protection.
     * Returns true if the call is allowed, false if circuit is open.
     * Throws CircuitBreakerOpenException if the circuit is open.
     */
    public <T> T execute(ThrowableSupplier<T> supplier) throws BlockException {
        ContextUtil.enter(GATEWAY_CONTEXT, RESOURCE_NAME);
        Entry entry = null;
        try {
            entry = SphU.entry(RESOURCE_NAME, EntryType.OUT);
            return supplier.get();
        } catch (BlockException e) {
            log.warn("Circuit breaker triggered for resource: {}, strategy: {}",
                    RESOURCE_NAME, CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getName());
            throw e;
        } catch (Throwable t) {
            Tracer.trace(t);
            throw t;
        } finally {
            if (entry != null) {
                entry.exit();
            }
            ContextUtil.exit();
        }
    }

    /**
     * Check if the circuit breaker is currently open for the backend interface.
     * Uses Sentinel's statistic node to determine the current state.
     */
    public boolean isCircuitOpen() {
        com.alibaba.csp.sentinel.node.ClusterNode clusterNode =
                com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot.getClusterNodeMap()
                        .get(RESOURCE_NAME);
        if (clusterNode == null) {
            return false;
        }
        long totalCount = clusterNode.totalCount();
        if (totalCount < circuitBreakerConfig.getMinRequestAmount()) {
            return false;
        }
        double avgRt = clusterNode.avgRt();
        double slowRatio = clusterNode.slowRatio();
        if (slowRatio >= circuitBreakerConfig.getSlowCallRatioThreshold() && avgRt > circuitBreakerConfig.getSlowCallDurationMs()) {
            log.warn("Circuit breaker slow-call threshold exceeded: slowRatio={}, avgRt={}ms", slowRatio, avgRt);
            return true;
        }
        return false;
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        T get() throws Throwable;
    }
}
