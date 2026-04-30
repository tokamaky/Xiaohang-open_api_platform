package com.xiaohang.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaohang.xiaohangapicommon.common.BaseResponse;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class GlobalFilterExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status = HttpStatus.FORBIDDEN;
        String message = ex.getMessage();

        if (ex instanceof BusinessException) {
            BusinessException be = (BusinessException) ex;
            int code = be.getCode();
            message = be.getMessage();

            if (code == ErrorCode.RATE_LIMIT_ERROR.getCode()) {
                status = HttpStatus.TOO_MANY_REQUESTS;
                response.getHeaders().add(HttpHeaders.RETRY_AFTER, "60");
            } else if (code == ErrorCode.NOT_LOGIN_ERROR.getCode()
                    || code == ErrorCode.NOT_FOUND_ERROR.getCode()) {
                status = HttpStatus.NOT_FOUND;
            } else if (code == ErrorCode.SYSTEM_ERROR.getCode()
                    || code == ErrorCode.OPERATION_ERROR.getCode()) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        } else if (ex instanceof ResponseStatusException) {
            status = ((ResponseStatusException) ex).getStatus();
        }

        response.setStatusCode(status);
        log.error("Gateway exception: code={}, msg={}, ex={}", status.value(), message, ex.getClass().getSimpleName());

        BaseResponse<String> fail = ResultUtils.fail(status.value(), message);
        return writeResponse(exchange, fail);
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, BaseResponse<String> body) {
        ServerHttpResponse response = exchange.getResponse();
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                byte[] bytes = objectMapper.writeValueAsBytes(body);
                return bufferFactory.wrap(bytes);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize error response", e);
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
