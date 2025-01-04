package com.xiaohang.xiaohangapicommon.common;

/**
 * 错误码
 *
 * @author xiaohang
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "Request parameter error"),
    NOT_LOGIN_ERROR(40100, "Not logged in"),
    NO_AUTH_ERROR(40101, "No permissions"),
    NOT_FOUND_ERROR(40400, "The requested data does not exist"),
    FORBIDDEN_ERROR(40300, "Access Denied"),
    SYSTEM_ERROR(50000, "System internal abnormality"),
    OPERATION_ERROR(50001, "Operation failed");

    private final int code;

    /**
     * errorcode message
     */
    private final String message;


    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
