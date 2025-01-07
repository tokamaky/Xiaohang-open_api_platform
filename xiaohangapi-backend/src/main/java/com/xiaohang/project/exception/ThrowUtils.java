package com.xiaohang.project.exception;


import com.xiaohang.xiaohangapicommon.common.ErrorCode;

/**
 * Exception Throwing Utility Class
 *
 */
public class ThrowUtils {

    /**
     * Throws an exception if the condition is true.
     *
     * @param condition The condition to check.
     * @param runtimeException The runtime exception to throw.
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * Throws an exception if the condition is true.
     *
     * @param condition The condition to check.
     * @param errorCode The error code to use when creating the exception.
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * Throws an exception if the condition is true.
     *
     * @param condition The condition to check.
     * @param errorCode The error code to use when creating the exception.
     * @param message The message to include in the exception.
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
