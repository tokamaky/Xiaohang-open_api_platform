package com.xiaohang.xiaohangapigateway;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import com.xiaohang.exception.BusinessException;
import com.xiaohang.xiaohangapiclientsdk.utils.SignUtils;
//import cn.hutool.core.net.URLDecoder;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.service.InnerInterfaceInfoService;
import com.xiaohang.xiaohangapicommon.service.InnerUserInterfaceInfoService;
import com.xiaohang.xiaohangapicommon.service.InnerUserService;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    private static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1", "127.0.0.2", "0:0:0:0:0:0:0:1");
    private static final String DYE_DATA_HEADER = "X-Dye-Data";
    private static final String DYE_DATA_VALUE = "nero";

    private static final long FIVE_MINUTES = 5 * 60 * 1000L;

    private static final String INTERFACE_HOST = "https://interface-production-b00a.up.railway.app";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String IP_ADDRESS = Objects.requireNonNull(request.getLocalAddress()).getHostString();
        String path = request.getPath().value();
        log.info("Request Unique Identifier: {}", request.getId());
        log.info("Request Path: {}", path);
        log.info("Request Parameters: {}", request.getQueryParams());
        log.info("Request Source Address (IP Address): {}", IP_ADDRESS);
        log.info("Request Source Address (Remote Address): {}", request.getRemoteAddress());


        ServerHttpResponse response = exchange.getResponse();

        // 2. 黑白名单
//        if (!IP_WHITE_LIST.contains(IP_ADDRESS)) {
//            log.error("Unauthorized IP Address: {}", IP_ADDRESS);
//            return handleNoAuth(response);
//        }

        // 3. 用户鉴权 （判断 accessKey 和 secretKey 是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String timestamp = headers.getFirst("timestamp");
        String nonce = headers.getFirst("nonce");
        String sign = headers.getFirst("sign");
        String body = URLUtil.decode(headers.getFirst("body"), CharsetUtil.CHARSET_UTF_8);
        String method = headers.getFirst("method");

        log.info("AccessKey: {}", accessKey);
        log.info("Timestamp: {}", timestamp);
        log.info("Nonce: {}", nonce);
        log.info("Sign: {}", sign);
        log.info("Method: {}", method);

        if (StringUtil.isEmpty(nonce)
                || StringUtil.isEmpty(sign)
                || StringUtil.isEmpty(timestamp)
                || StringUtil.isEmpty(method)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "The request header parameters are incomplete");
        }


// Query if the user exists using the accessKey
        User invokeUser = innerUserService.getInvokeUser(accessKey);
        if (invokeUser == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Invalid accessKey!");
        }

// Check if the nonce exists to prevent replay attacks
//        String existNonce = (String) redisTemplate.opsForValue().get(nonce);
//        if (StringUtil.isNotBlank(existNonce)) {
//            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Duplicate request!");
//        }

// Ensure the timestamp is within 5 minutes (300,000 milliseconds) of the current time
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        long difference = currentTimeMillis - Long.parseLong(timestamp);
        if (Math.abs(difference) > 300000) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Request timeout!");
        }

// Validate the signature
// Use the secretKey fetched with the accessKey to generate the server-side sign
// Compare the generated sign with the one provided by the client
        String serverSign = SignUtils.genSign(body, invokeUser.getSecretKey());
        if (!sign.equals(serverSign)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "Invalid signature!");
        }

// Check if the requested mock interface exists
// Query the database to verify the existence of the interface and match the method (also validate the request parameters)
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("Error fetching interface details", e);
        }
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface does not exist!");
        }

// Forward the request to invoke the mock interface and log the response
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());

    }
    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long userId, long interfaceInfoId) {
        try {
            // 从交换机拿到原始response
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 缓冲区工厂 拿到缓存数据
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 拿到状态码
            HttpStatus statusCode = (HttpStatus) originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                // 装饰，增强能力
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    // 等调用完转发的接口后才会执行
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        // 对象是响应式的
                        if (body instanceof Flux) {
                            // 我们拿到真正的body
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 往返回值里面写数据
                            // 拼接字符串
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                // 7. 调用成功，接口调用次数+1
                                try {
                                    innerUserInterfaceInfoService.invokeCount(userId, interfaceInfoId);
                                } catch (Exception e) {
                                    log.error("invokeInterfaceCount error", e);
                                }
                                // data从这个content中读取
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);// 释放掉内存
                                // 6.构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                String data = new String(content, StandardCharsets.UTF_8);// data
                                rspArgs.add(data);
                                sb2.append(data);
                                // 打印日志
                                log.info("response result：" + data);
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            // 8.调用失败返回错误状态码
                            log.error("<--- {} Response Code Exception", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 流量染色，只有染色数据才能被调用
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header(DYE_DATA_HEADER, DYE_DATA_VALUE)
                        .build();

                ServerWebExchange serverWebExchange = exchange.mutate()
                        .request(modifiedRequest)
                        .response(decoratedResponse)
                        .build();
                return chain.filter(serverWebExchange);
            }
            return chain.filter(exchange);// 降级处理返回数据
        } catch (Exception e) {
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }

    }

    private Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.setRawStatusCode(HttpStatus.FORBIDDEN.value());
        return response.setComplete();
    }

    private Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }


}
