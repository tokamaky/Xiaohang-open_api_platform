package com.xiaohang.project.aop;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;
/**
 * Login Check AOP (Aspect-Oriented Programming)
 * @author Xiaohang
 */
@Aspect
@Component
@Slf4j
public class LogInterceptor {

    /**
     * Execute Interception
     */
    @Around("execution(* com.xiaohang.project.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // Start timing
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // Get request path
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();

        // Generate unique request id
        String requestId = UUID.randomUUID().toString();
        String url = httpServletRequest.getRequestURI();

        // Get request parameters
        Object[] args = point.getArgs();
        String reqParam = "[" + StringUtils.join(args, ", ") + "]";

        // Log the request details
        log.info("request start, id: {}, path: {}, ip: {}, params: {}", requestId, url,
                httpServletRequest.getRemoteHost(), reqParam);

        // Proceed with the original method execution
        Object result = point.proceed();

        // Log the response details
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        log.info("request end, id: {}, cost: {}ms", requestId, totalTimeMillis);

        return result;
    }
}
