package com.xiaohang.xiaohangapiinterface.controller;

import com.xiaohang.xiaohangapiclientsdk.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * Unit tests for NameController
 * Tests API endpoint logic without database dependency
 */
@RunWith(MockitoJUnitRunner.class)
public class NameControllerTest {

    @InjectMocks
    private NameController nameController;

    /**
     * Test getNameByGet endpoint
     */
    @Test
    public void testGetNameByGet() {
        String result = nameController.getNameByGet("John");
        assertEquals("GET your name is: John", result);
    }

    /**
     * Test getNameByGet with empty name
     */
    @Test
    public void testGetNameByGet_EmptyName() {
        String result = nameController.getNameByGet("");
        assertEquals("GET your name is: ", result);
    }

    /**
     * Test getNameByGet with special characters
     */
    @Test
    public void testGetNameByGet_SpecialCharacters() {
        String result = nameController.getNameByGet("张三");
        assertEquals("GET your name is: 张三", result);
    }

    /**
     * Test getNameByPost endpoint
     */
    @Test
    public void testGetNameByPost() {
        String result = nameController.getNameByPost("Jane");
        assertEquals("POST 你的名字是Jane", result);
    }

    /**
     * Test getNameByPost with empty name
     */
    @Test
    public void testGetNameByPost_EmptyName() {
        String result = nameController.getNameByPost("");
        assertEquals("POST 你的名字是", result);
    }

    /**
     * Test getNameByPost with Chinese name
     */
    @Test
    public void testGetNameByPost_ChineseName() {
        String result = nameController.getNameByPost("小明");
        assertEquals("POST 你的名字是小明", result);
    }

    /**
     * Test getUserNameByPost endpoint
     */
    @Test
    public void testGetUserNameByPost() {
        User user = new User();
        user.setUsername("testuser");

        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = nameController.getUserNameByPost(user, request);
        assertTrue(result.contains("testuser"));
    }

    /**
     * Test getUserNameByPost with null username
     */
    @Test
    public void testGetUserNameByPost_NullUsername() {
        User user = new User();
        user.setUsername(null);

        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = nameController.getUserNameByPost(user, request);
        assertTrue(result.contains("null"));
    }

    /**
     * Test getUserNameByPost with empty username
     */
    @Test
    public void testGetUserNameByPost_EmptyUsername() {
        User user = new User();
        user.setUsername("");

        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = nameController.getUserNameByPost(user, request);
        assertTrue(result.contains(""));
    }
}
