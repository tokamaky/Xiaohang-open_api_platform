package com.xiaohang.xiaohangapicommon.common;

/*
        * Utility class for returning responses.
        *
        * Author: Xiaohang Ji
        */
public class ResultUtils {
    /**
     * Success response.
     *
     * @param data the response data
     * @param <T>  the type of the response data
     * @return a successful BaseResponse containing the data
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * Failure response.
     *
     * @param errorCode the error code object
     * @return a BaseResponse containing the error details
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * Failure response.
     *
     * @param code    the error code
     * @param message the error message
     * @return a BaseResponse containing the error details
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * Failure response with custom message.
     *
     * @param errorCode the error code object
     * @param message   the custom error message
     * @return a BaseResponse containing the error details
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }

    /**
     * Success response with data and a default success message.
     *
     * @param data the response data
     * @param <T>  the type of the response data
     * @return a successful BaseResponse containing the data
     */
    public static <T> BaseResponse<T> dataOk(T data) {
        return new BaseResponse<T>(0, data, "success");
    }

    /**
     * General failure response with a default system error message.
     *
     * @param <T> the type of the response data
     * @return a BaseResponse indicating a system error
     */
    public static <T> BaseResponse<T> fail() {
        return new BaseResponse<T>(50001, "system error");
    }

    /**
     * Failure response with a specific error code.
     *
     * @param errorCode the error code object
     * @param <T>       the type of the response data
     * @return a BaseResponse containing the error details
     */
    public static <T> BaseResponse<T> fail(ErrorCode errorCode) {
        return new BaseResponse<T>(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * Failure response with custom data and a message.
     *
     * @param data    the custom data
     * @param message the error message
     * @param <T>     the type of the response data
     * @return a BaseResponse containing the error details
     */
    public static <T> BaseResponse<T> fail(T data, String message) {
        return new BaseResponse<T>(50001, data, message);
    }

    /**
     * Failure response with a custom code and message.
     *
     * @param code    the error code
     * @param message the error message
     * @param <T>     the type of the response data
     * @return a BaseResponse containing the error details
     */
    public static <T> BaseResponse<T> fail(int code, String message) {
        return new BaseResponse<T>(code, null, message);
    }

    /**
     * Failure response with a custom message.
     *
     * @param message the error message
     * @param <T>     the type of the response data
     * @return a BaseResponse containing the error details
     */
    public static <T> BaseResponse<T> fail(String message) {
        return new BaseResponse<T>(50000, null, message);
    }


}
