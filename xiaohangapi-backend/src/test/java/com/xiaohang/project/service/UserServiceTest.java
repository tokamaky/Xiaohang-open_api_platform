package com.xiaohang.project.service;

import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.service.impl.UserServiceImpl;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.model.dto.user.UserQueryRequest;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.enums.UserRoleEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * Unit tests for UserService
 * Tests business logic methods without database dependency
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private com.xiaohang.project.utils.JwtUtils jwtUtils;

    /**
     * Test userRegister - empty parameters
     */
    @Test
    public void testUserRegister_EmptyAccount() {
        try {
            userService.userRegister("", "password123", "password123");
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("empty"));
        }
    }

    @Test
    public void testUserRegister_EmptyPassword() {
        try {
            userService.userRegister("testuser", "", "password123");
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testUserRegister_PasswordMismatch() {
        try {
            userService.userRegister("testuser", "password123", "password456");
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("match"));
        }
    }

    @Test
    public void testUserRegister_ShortAccount() {
        try {
            userService.userRegister("usr", "password123", "password123");
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("too short"));
        }
    }

    @Test
    public void testUserRegister_ShortPassword() {
        try {
            userService.userRegister("testuser", "pass", "pass");
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("too short"));
        }
    }

    /**
     * Test userLogin - empty parameters
     */
    @Test
    public void testUserLogin_EmptyAccount() {
        try {
            userService.userLogin("", "password123", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testUserLogin_EmptyPassword() {
        try {
            userService.userLogin("testuser", "", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testUserLogin_ShortAccount() {
        try {
            userService.userLogin("usr", "password123", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testUserLogin_ShortPassword() {
        try {
            userService.userLogin("testuser", "pass123", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * Test isAdmin with null user
     */
    @Test
    public void testIsAdmin_NullUser() {
        assertFalse(userService.isAdmin((User) null));
    }

    /**
     * Test isAdmin with admin user
     */
    @Test
    public void testIsAdmin_AdminUser() {
        User adminUser = new User();
        adminUser.setUserRole(UserRoleEnum.ADMIN.getValue());
        assertTrue(userService.isAdmin(adminUser));
    }

    /**
     * Test isAdmin with normal user
     */
    @Test
    public void testIsAdmin_NormalUser() {
        User normalUser = new User();
        normalUser.setUserRole(UserRoleEnum.USER.getValue());
        assertFalse(userService.isAdmin(normalUser));
    }

    /**
     * Test getLoginUserVO with null user
     */
    @Test
    public void testGetLoginUserVO_NullUser() {
        assertNull(userService.getLoginUserVO(null));
    }

    /**
     * Test getUserVO with null user
     */
    @Test
    public void testGetUserVO_NullUser() {
        assertNull(userService.getUserVO((User) null));
    }

    /**
     * Test getUserVO with valid user
     */
    @Test
    public void testGetUserVO_ValidUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("TestUser");
        user.setUserAccount("testaccount");
        user.setUserRole(UserRoleEnum.USER.getValue());

        com.xiaohang.xiaohangapicommon.model.vo.UserVO userVO = userService.getUserVO(user);
        assertNotNull(userVO);
        assertEquals(user.getId(), userVO.getId());
    }

    /**
     * Test getUserVOList with null list
     */
    @Test
    public void testGetUserVOList_NullList() {
        assertNotNull(userService.getUserVO((java.util.List<User>) null));
    }

    /**
     * Test getQueryWrapper with null request
     */
    @Test(expected = BusinessException.class)
    public void testGetQueryWrapper_NullRequest() {
        userService.getQueryWrapper(null);
    }

    /**
     * Test getQueryWrapper with valid request
     */
    @Test
    public void testGetQueryWrapper_ValidRequest() {
        UserQueryRequest request = new UserQueryRequest();
        request.setUserName("test");
        request.setSortField("createTime");
        request.setSortOrder("desc");

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> wrapper = userService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    /**
     * Test changePassword with empty parameters
     */
    @Test
    public void testChangePassword_EmptyOldPassword() {
        try {
            userService.changePassword("", "newpassword123", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testChangePassword_EmptyNewPassword() {
        try {
            userService.changePassword("oldpassword123", "", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testChangePassword_ShortNewPassword() {
        try {
            userService.changePassword("oldpassword123", "new123", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("8 characters"));
        }
    }

    /**
     * Test setPassword with empty parameter
     */
    @Test
    public void testSetPassword_EmptyPassword() {
        try {
            userService.setPassword("", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    @Test
    public void testSetPassword_ShortPassword() {
        try {
            userService.setPassword("short", null);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * Test updateSecretKey with null user
     */
    @Test(expected = NullPointerException.class)
    public void testUpdateSecretKey_NullUser() {
        userService.updateSecretKey(1L);
    }
}
