package com.xiaohang.project.common;

/*
        * Utility class for returning responses.
        *
        * Author: Xiaohang Ji
        */
public class ResultUtils {

    /**
     * Success response
     *
     * @param data The data to return
     * @param <T> The type of the data
     * @return A successful response
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * Error response
     *
     * @param errorCode The error code
     * @return A failure response
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * Error response with custom code and message
     *
     * @param code The error code
     * @param message The error message
     * @return A failure response
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * Error response with custom error code and message
     *
     * @param errorCode The error code
     * @param message The error message
     * @return A failure response
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
