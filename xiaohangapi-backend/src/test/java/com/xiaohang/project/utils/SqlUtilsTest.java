package com.xiaohang.project.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for SqlUtils
 * Tests SQL validation utility methods
 */
public class SqlUtilsTest {

    /**
     * Test validSortField with valid field names
     */
    @Test
    public void testValidSortField_ValidFields() {
        assertTrue(SqlUtils.validSortField("id"));
        assertTrue(SqlUtils.validSortField("createTime"));
        assertTrue(SqlUtils.validSortField("updateTime"));
        assertTrue(SqlUtils.validSortField("userName"));
        assertTrue(SqlUtils.validSortField("userAccount"));
    }

    /**
     * Test validSortField with SQL injection attempts
     */
    @Test
    public void testValidSortField_SqlInjection() {
        // SQL injection attempts should be rejected
        assertFalse(SqlUtils.validSortField("; DROP TABLE users;"));
        assertFalse(SqlUtils.validSortField("1=1"));
        assertFalse(SqlUtils.validSortField("' OR '1'='1"));
        assertFalse(SqlUtils.validSortField("id; DELETE FROM users"));
        assertFalse(SqlUtils.validSortField("id --"));
        assertFalse(SqlUtils.validSortField("id /* */"));
        assertFalse(SqlUtils.validSortField("UNION SELECT"));
        assertFalse(SqlUtils.validSortField("id' OR '1'='1"));
    }

    /**
     * Test validSortField with null input
     */
    @Test
    public void testValidSortField_NullInput() {
        assertFalse(SqlUtils.validSortField(null));
    }

    /**
     * Test validSortField with empty string
     */
    @Test
    public void testValidSortField_EmptyString() {
        assertFalse(SqlUtils.validSortField(""));
    }

    /**
     * Test validSortField with spaces
     */
    @Test
    public void testValidSortField_WithSpaces() {
        assertFalse(SqlUtils.validSortField("   "));
        assertFalse(SqlUtils.validSortField(" id "));
    }

    /**
     * Test validSortField with special characters
     */
    @Test
    public void testValidSortField_SpecialCharacters() {
        assertFalse(SqlUtils.validSortField("id@email.com"));
        assertFalse(SqlUtils.validSortField("id#123"));
        assertFalse(SqlUtils.validSortField("id$100"));
        assertFalse(SqlUtils.validSortField("id%20"));
    }

    /**
     * Test validSortField with numeric input
     */
    @Test
    public void testValidSortField_NumericInput() {
        assertFalse(SqlUtils.validSortField("123"));
        assertFalse(SqlUtils.validSortField("0"));
    }

    /**
     * Test validSortField with unicode characters
     */
    @Test
    public void testValidSortField_UnicodeCharacters() {
        assertFalse(SqlUtils.validSortField("用户名"));
        assertFalse(SqlUtils.validSortField("name;中文"));
    }

    /**
     * Test validSortField with long strings
     */
    @Test
    public void testValidSortField_LongString() {
        // Very long string should be rejected
        String longString = "a".repeat(100);
        assertFalse(SqlUtils.validSortField(longString));
    }

    /**
     * Test validSortField with case sensitivity
     */
    @Test
    public void testValidSortField_CaseSensitivity() {
        // Field names are case-sensitive
        assertTrue(SqlUtils.validSortField("createTime"));
        assertFalse(SqlUtils.validSortField("CREATETIME"));
        assertFalse(SqlUtils.validSortField("CreateTime"));
    }
}
