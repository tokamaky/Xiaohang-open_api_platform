package com.xiaohang.project.exception;

import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for BusinessException
 * Tests exception creation and error code handling
 */
public class BusinessExceptionTest {

    /**
     * Test constructor with code and message
     */
    @Test
    public void testConstructor_WithCodeAndMessage() {
        BusinessException exception = new BusinessException(40000, "Test error message");
        assertEquals(40000, exception.getCode());
        assertEquals("Test error message", exception.getMessage());
    }

    /**
     * Test constructor with ErrorCode only
     */
    @Test
    public void testConstructor_WithErrorCode() {
        BusinessException exception = new BusinessException(ErrorCode.PARAMS_ERROR);
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals(ErrorCode.PARAMS_ERROR.getMessage(), exception.getMessage());
    }

    /**
     * Test constructor with ErrorCode and custom message
     */
    @Test
    public void testConstructor_WithErrorCodeAndMessage() {
        String customMessage = "Custom message";
        BusinessException exception = new BusinessException(ErrorCode.PARAMS_ERROR, customMessage);
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertEquals(customMessage, exception.getMessage());
    }

    /**
     * Test all error codes
     */
    @Test
    public void testAllErrorCodes() {
        assertEquals(0, ErrorCode.SUCCESS.getCode());
        assertEquals("ok", ErrorCode.SUCCESS.getMessage());

        assertEquals(40000, ErrorCode.PARAMS_ERROR.getCode());
        assertEquals("Request parameter error", ErrorCode.PARAMS_ERROR.getMessage());

        assertEquals(40100, ErrorCode.NOT_LOGIN_ERROR.getCode());
        assertEquals("Not logged in", ErrorCode.NOT_LOGIN_ERROR.getMessage());

        assertEquals(40101, ErrorCode.NO_AUTH_ERROR.getCode());
        assertEquals("No permissions", ErrorCode.NO_AUTH_ERROR.getMessage());

        assertEquals(40102, ErrorCode.FIRST_TIME_LOGIN.getCode());
        assertEquals("Welcome, Please login", ErrorCode.FIRST_TIME_LOGIN.getMessage());

        assertEquals(40400, ErrorCode.NOT_FOUND_ERROR.getCode());
        assertEquals("The requested data does not exist", ErrorCode.NOT_FOUND_ERROR.getMessage());

        assertEquals(40300, ErrorCode.FORBIDDEN_ERROR.getCode());
        assertEquals("Access Denied", ErrorCode.FORBIDDEN_ERROR.getMessage());

        assertEquals(50000, ErrorCode.SYSTEM_ERROR.getCode());
        assertEquals("System internal abnormality", ErrorCode.SYSTEM_ERROR.getMessage());

        assertEquals(50001, ErrorCode.OPERATION_ERROR.getCode());
        assertEquals("Operation failed", ErrorCode.OPERATION_ERROR.getMessage());

        assertEquals(42900, ErrorCode.RATE_LIMIT_ERROR.getCode());
        assertEquals("Rate limit exceeded, please try again later", ErrorCode.RATE_LIMIT_ERROR.getMessage());

        assertEquals(50300, ErrorCode.CIRCUIT_BREAKER_ERROR.getCode());
        assertEquals("Service temporarily unavailable, please try again later", ErrorCode.CIRCUIT_BREAKER_ERROR.getMessage());
    }

    /**
     * Test exception inheritance from RuntimeException
     */
    @Test
    public void testExceptionInheritance() {
        BusinessException exception = new BusinessException(ErrorCode.SYSTEM_ERROR);
        assertTrue(exception instanceof RuntimeException);
        assertTrue(exception instanceof Exception);
    }

    /**
     * Test exception with null message
     */
    @Test
    public void testConstructor_WithNullMessage() {
        BusinessException exception = new BusinessException(50000, null);
        assertEquals(50000, exception.getCode());
        assertNull(exception.getMessage());
    }

    /**
     * Test exception message can be retrieved via getMessage
     */
    @Test
    public void testGetMessage() {
        String message = "Test exception message";
        BusinessException exception = new BusinessException(40000, message);
        assertEquals(message, exception.getMessage());
    }

    /**
     * Test getCode returns correct code
     */
    @Test
    public void testGetCode() {
        int expectedCode = 12345;
        BusinessException exception = new BusinessException(expectedCode, "test");
        assertEquals(expectedCode, exception.getCode());
    }

    /**
     * Test exception stack trace
     */
    @Test
    public void testStackTrace() {
        BusinessException exception = new BusinessException(ErrorCode.SYSTEM_ERROR);
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }
}
