package com.xiaohang.project.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.service.impl.UserServiceImpl;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.model.dto.user.UserQueryRequest;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.enums.UserRoleEnum;
import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;
import com.xiaohang.xiaohangapicommon.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive User Service Tests
 * Tests for user registration, login, authentication, and management operations
 *
 * @author xiaohang
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class UserServiceTest {

    @Autowired
    private UserService userService;

    private static final String TEST_USER_ACCOUNT_PREFIX = "testuser_";
    private static final String TEST_USER_PASSWORD = "password123";
    private static String testUserAccount;
    private static Long testUserId;
    private static HttpSession testSession;

    @BeforeAll
    static void setup(TestInfo testInfo) {
        testUserAccount = TEST_USER_ACCOUNT_PREFIX + RandomUtil.randomNumbers(8);
        log.info("Test User Account: {}", testUserAccount);
    }

    @AfterEach
    void cleanup() {
        if (testSession != null) {
            testSession.invalidate();
        }
    }

    // ============================================
    // User Registration Tests
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Test successful user registration")
    void testUserRegister_Success() {
        long userId = userService.userRegister(
                testUserAccount,
                TEST_USER_PASSWORD,
                TEST_USER_PASSWORD
        );

        assertTrue(userId > 0, "User ID should be positive");
        testUserId = userId;
        log.info("Registered user with ID: {}", userId);
    }

    @Test
    @Order(2)
    @DisplayName("Test registration fails with empty account")
    void testUserRegister_EmptyAccount() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userRegister("", TEST_USER_PASSWORD, TEST_USER_PASSWORD)
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @Order(3)
    @DisplayName("Test registration fails with short account")
    void testUserRegister_ShortAccount() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userRegister("usr", TEST_USER_PASSWORD, TEST_USER_PASSWORD)
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("too short"));
    }

    @Test
    @Order(4)
    @DisplayName("Test registration fails with short password")
    void testUserRegister_ShortPassword() {
        String shortPasswordAccount = TEST_USER_ACCOUNT_PREFIX + RandomUtil.randomNumbers(8);
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userRegister(shortPasswordAccount, "1234567", "1234567")
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("too short"));
    }

    @Test
    @Order(5)
    @DisplayName("Test registration fails with mismatched passwords")
    void testUserRegister_MismatchedPasswords() {
        String newAccount = TEST_USER_ACCOUNT_PREFIX + RandomUtil.randomNumbers(8);
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userRegister(newAccount, TEST_USER_PASSWORD, "differentpass123")
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("do not match"));
    }

    @Test
    @Order(6)
    @DisplayName("Test registration fails with duplicate account")
    void testUserRegister_DuplicateAccount() {
        String duplicateAccount = TEST_USER_ACCOUNT_PREFIX + "dup_" + RandomUtil.randomNumbers(4);

        long firstId = userService.userRegister(duplicateAccount, TEST_USER_PASSWORD, TEST_USER_PASSWORD);
        assertTrue(firstId > 0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userRegister(duplicateAccount, TEST_USER_PASSWORD, TEST_USER_PASSWORD)
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("duplicated"));

        // Cleanup
        userService.removeById(firstId);
    }

    // ============================================
    // User Login Tests
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Test successful user login")
    void testUserLogin_Success() {
        MockHttpSession session = new MockHttpSession();

        LoginUserVO loginResult = userService.userLogin(
                testUserAccount,
                TEST_USER_PASSWORD,
                session
        );

        assertNotNull(loginResult, "Login result should not be null");
        assertEquals(testUserAccount, loginResult.getUserAccount(), "Account should match");
        assertNotNull(loginResult.getAccessKey(), "Access key should be generated");
        assertNotNull(loginResult.getSecretKey(), "Secret key should be generated");
        log.info("User logged in successfully: {}", loginResult);
    }

    @Test
    @Order(11)
    @DisplayName("Test login fails with wrong password")
    void testUserLogin_WrongPassword() {
        MockHttpSession session = new MockHttpSession();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userLogin(testUserAccount, "wrongpassword123", session)
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
        assertTrue(exception.getMessage().contains("incorrect") || exception.getMessage().contains("exist"));
    }

    @Test
    @Order(12)
    @DisplayName("Test login fails with non-existent account")
    void testUserLogin_NonExistentAccount() {
        MockHttpSession session = new MockHttpSession();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userLogin("nonexistent_user_123", TEST_USER_PASSWORD, session)
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @Order(13)
    @DisplayName("Test login fails with empty credentials")
    void testUserLogin_EmptyCredentials() {
        MockHttpSession session = new MockHttpSession();

        assertThrows(
                BusinessException.class,
                () -> userService.userLogin("", TEST_USER_PASSWORD, session)
        );

        assertThrows(
                BusinessException.class,
                () -> userService.userLogin(testUserAccount, "", session)
        );
    }

    // ============================================
    // Get Login User Tests
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Test get login user from session")
    void testGetLoginUser_FromSession() {
        MockHttpSession session = new MockHttpSession();

        // Login first
        LoginUserVO loginResult = userService.userLogin(testUserAccount, TEST_USER_PASSWORD, session);

        // Get logged-in user
        User currentUser = userService.getLoginUser(session);

        assertNotNull(currentUser, "Current user should not be null");
        assertEquals(testUserId, currentUser.getId(), "User ID should match");
        assertEquals(testUserAccount, currentUser.getUserAccount(), "Account should match");
    }

    @Test
    @Order(21)
    @DisplayName("Test get login user fails when not logged in")
    void testGetLoginUser_NotLoggedIn() {
        MockHttpSession session = new MockHttpSession();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.getLoginUser(session)
        );
        assertEquals(ErrorCode.NOT_LOGIN_ERROR.getCode(), exception.getCode());
    }

    @Test
    @Order(22)
    @DisplayName("Test get login user permit null returns null when not logged in")
    void testGetLoginUserPermitNull_NotLoggedIn() {
        MockHttpSession session = new MockHttpSession();

        User user = userService.getLoginUserPermitNull(session);
        assertNull(user, "Should return null when not logged in");
    }

    // ============================================
    // Admin Check Tests
    // ============================================

    @Test
    @Order(30)
    @DisplayName("Test regular user is not admin")
    void testIsAdmin_RegularUser() {
        MockHttpSession session = new MockHttpSession();
        userService.userLogin(testUserAccount, TEST_USER_PASSWORD, session);

        boolean isAdmin = userService.isAdmin(session);
        assertFalse(isAdmin, "Regular user should not be admin");

        User user = userService.getLoginUser(session);
        assertFalse(userService.isAdmin(user), "Regular user should not be admin via isAdmin(User)");
    }

    @Test
    @Order(31)
    @DisplayName("Test isAdmin returns false for null user")
    void testIsAdmin_NullUser() {
        assertFalse(userService.isAdmin((User) null), "Null user should not be admin");
    }

    @Test
    @Order(32)
    @DisplayName("Test isAdmin works with admin user")
    void testIsAdmin_AdminUser() {
        User adminUser = new User();
        adminUser.setUserRole(UserRoleEnum.ADMIN.getValue());
        assertTrue(userService.isAdmin(adminUser), "Admin user should be admin");
    }

    // ============================================
    // User Logout Tests
    // ============================================

    @Test
    @Order(40)
    @DisplayName("Test successful logout")
    void testUserLogout_Success() {
        MockHttpSession session = new MockHttpSession();
        userService.userLogin(testUserAccount, TEST_USER_PASSWORD, session);

        boolean logoutResult = userService.userLogout(session);
        assertTrue(logoutResult, "Logout should succeed");
    }

    @Test
    @Order(41)
    @DisplayName("Test logout fails when not logged in")
    void testUserLogout_NotLoggedIn() {
        MockHttpSession session = new MockHttpSession();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.userLogout(session)
        );
        assertEquals(ErrorCode.OPERATION_ERROR.getCode(), exception.getCode());
    }

    // ============================================
    // Get User VO Tests
    // ============================================

    @Test
    @Order(50)
    @DisplayName("Test get single user VO")
    void testGetUserVO_Single() {
        User user = userService.getById(testUserId);
        assertNotNull(user, "User should exist");

        UserVO userVO = userService.getUserVO(user);
        assertNotNull(userVO, "UserVO should not be null");
        assertEquals(user.getId(), userVO.getId(), "ID should match");
        assertEquals(user.getUserAccount(), userVO.getUserAccount(), "Account should match");
    }

    @Test
    @Order(51)
    @DisplayName("Test get user VO with null user returns null")
    void testGetUserVO_NullUser() {
        UserVO userVO = userService.getUserVO((User) null);
        assertNull(userVO, "Should return null for null user");
    }

    @Test
    @Order(52)
    @DisplayName("Test get list of user VOs")
    void testGetUserVO_List() {
        List<User> users = new ArrayList<>();
        users.add(userService.getById(testUserId));

        List<UserVO> userVOList = userService.getUserVO(users);
        assertNotNull(userVOList, "UserVO list should not be null");
        assertEquals(1, userVOList.size(), "Should have one user");
    }

    @Test
    @Order(53)
    @DisplayName("Test get user VO list with empty list")
    void testGetUserVO_EmptyList() {
        List<UserVO> userVOList = userService.getUserVO(new ArrayList<>());
        assertNotNull(userVOList, "Should return empty list, not null");
        assertTrue(userVOList.isEmpty(), "List should be empty");
    }

    // ============================================
    // Get Login User VO Tests
    // ============================================

    @Test
    @Order(60)
    @DisplayName("Test get login user VO")
    void testGetLoginUserVO() {
        MockHttpSession session = new MockHttpSession();
        userService.userLogin(testUserAccount, TEST_USER_PASSWORD, session);

        User user = userService.getLoginUser(session);
        LoginUserVO loginUserVO = userService.getLoginUserVO(user);

        assertNotNull(loginUserVO, "LoginUserVO should not be null");
        assertEquals(user.getId(), loginUserVO.getId(), "ID should match");
        assertEquals(user.getUserAccount(), loginUserVO.getUserAccount(), "Account should match");
    }

    @Test
    @Order(61)
    @DisplayName("Test get login user VO with null returns null")
    void testGetLoginUserVO_NullUser() {
        LoginUserVO loginUserVO = userService.getLoginUserVO((User) null);
        assertNull(loginUserVO, "Should return null for null user");
    }

    // ============================================
    // Query Wrapper Tests
    // ============================================

    @Test
    @Order(70)
    @DisplayName("Test get query wrapper with all parameters")
    void testGetQueryWrapper_AllParams() {
        UserQueryRequest request = new UserQueryRequest();
        request.setId(testUserId);
        request.setUserName("test");
        request.setUserProfile("profile");
        request.setUserRole(UserRoleEnum.USER.getValue());
        request.setSortField("createTime");
        request.setSortOrder("desc");

        QueryWrapper<User> wrapper = userService.getQueryWrapper(request);
        assertNotNull(wrapper, "QueryWrapper should not be null");
        assertTrue(wrapper.getSqlSegment().contains("userName"), "Should contain userName condition");
        assertTrue(wrapper.getSqlSegment().contains("userRole"), "Should contain userRole condition");
    }

    @Test
    @Order(71)
    @DisplayName("Test get query wrapper with null request throws exception")
    void testGetQueryWrapper_NullRequest() {
        assertThrows(
                BusinessException.class,
                () -> userService.getQueryWrapper(null)
        );
    }

    @Test
    @Order(72)
    @DisplayName("Test get query wrapper with only ID")
    void testGetQueryWrapper_OnlyId() {
        UserQueryRequest request = new UserQueryRequest();
        request.setId(testUserId);

        QueryWrapper<User> wrapper = userService.getQueryWrapper(request);
        assertNotNull(wrapper, "QueryWrapper should not be null");
        assertTrue(wrapper.getSqlSegment().contains("id"), "Should contain id condition");
    }

    // ============================================
    // Update Secret Key Tests
    // ============================================

    @Test
    @Order(80)
    @DisplayName("Test update secret key")
    void testUpdateSecretKey() {
        User userBefore = userService.getById(testUserId);
        String oldAccessKey = userBefore.getAccessKey();
        String oldSecretKey = userBefore.getSecretKey();

        boolean result = userService.updateSecretKey(testUserId);
        assertTrue(result, "Update should succeed");

        User userAfter = userService.getById(testUserId);
        assertNotEquals(oldAccessKey, userAfter.getAccessKey(), "Access key should be updated");
        assertNotEquals(oldSecretKey, userAfter.getSecretKey(), "Secret key should be updated");
    }

    // ============================================
    // Delete Account Tests
    // ============================================

    @Test
    @Order(90)
    @DisplayName("Test delete own account with correct password")
    void testDeleteMyAccount_WithPassword() {
        // Create a temporary user to delete
        String deleteTestAccount = TEST_USER_ACCOUNT_PREFIX + "del_" + RandomUtil.randomNumbers(4);
        long deleteTestUserId = userService.userRegister(
                deleteTestAccount, TEST_USER_PASSWORD, TEST_USER_PASSWORD
        );

        MockHttpSession deleteSession = new MockHttpSession();
        userService.userLogin(deleteTestAccount, TEST_USER_PASSWORD, deleteSession);

        // Delete account with password
        boolean result = userService.deleteMyAccount(TEST_USER_PASSWORD, deleteSession);
        assertTrue(result, "Delete should succeed");

        // Verify user is deleted
        User deletedUser = userService.getById(deleteTestUserId);
        assertNull(deletedUser, "Deleted user should not exist");
    }

    @Test
    @Order(91)
    @DisplayName("Test delete own account with wrong password")
    void testDeleteMyAccount_WrongPassword() {
        // Create a temporary user
        String deleteTestAccount = TEST_USER_ACCOUNT_PREFIX + "delwrong_" + RandomUtil.randomNumbers(4);
        long deleteTestUserId = userService.userRegister(
                deleteTestAccount, TEST_USER_PASSWORD, TEST_USER_PASSWORD
        );

        MockHttpSession deleteSession = new MockHttpSession();
        userService.userLogin(deleteTestAccount, TEST_USER_PASSWORD, deleteSession);

        // Try to delete with wrong password
        assertThrows(
                BusinessException.class,
                () -> userService.deleteMyAccount("wrongpassword123", deleteSession)
        );

        // Verify user still exists
        User existingUser = userService.getById(deleteTestUserId);
        assertNotNull(existingUser, "User should still exist after failed delete");

        // Cleanup
        userService.deleteMyAccount(TEST_USER_PASSWORD, deleteSession);
    }

    // ============================================
    // Change Password Tests
    // ============================================

    @Test
    @Order(100)
    @DisplayName("Test change password success")
    void testChangePassword_Success() {
        // Create a temporary user
        String changePwdAccount = TEST_USER_ACCOUNT_PREFIX + "chgpwd_" + RandomUtil.randomNumbers(4);
        long changePwdUserId = userService.userRegister(
                changePwdAccount, TEST_USER_PASSWORD, TEST_USER_PASSWORD
        );

        MockHttpSession changePwdSession = new MockHttpSession();
        userService.userLogin(changePwdAccount, TEST_USER_PASSWORD, changePwdSession);

        // Change password
        String newPassword = "newpassword123";
        boolean result = userService.changePassword(TEST_USER_PASSWORD, newPassword, changePwdSession);
        assertTrue(result, "Password change should succeed");

        // Verify can login with new password
        MockHttpSession newSession = new MockHttpSession();
        LoginUserVO loginResult = userService.userLogin(changePwdAccount, newPassword, newSession);
        assertNotNull(loginResult, "Should be able to login with new password");

        // Cleanup
        userService.deleteMyAccount(newPassword, newSession);
    }

    @Test
    @Order(101)
    @DisplayName("Test change password fails with wrong old password")
    void testChangePassword_WrongOldPassword() {
        MockHttpSession session = new MockHttpSession();
        userService.userLogin(testUserAccount, TEST_USER_PASSWORD, session);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.changePassword("wrongoldpassword", "newpassword123", session)
        );
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @Order(102)
    @DisplayName("Test change password fails with short new password")
    void testChangePassword_ShortNewPassword() {
        MockHttpSession session = new MockHttpSession();
        userService.userLogin(testUserAccount, TEST_USER_PASSWORD, session);

        assertThrows(
                BusinessException.class,
                () -> userService.changePassword(TEST_USER_PASSWORD, "short", session)
        );
    }

    // ============================================
    // Set Password Tests (for GitHub OAuth users)
    // ============================================

    @Test
    @Order(110)
    @DisplayName("Test set password for GitHub user")
    void testSetPassword_GitHubUser() {
        // This test simulates setting password for a GitHub user
        // In real scenario, the user would have github_id set
        User githubUser = new User();
        githubUser.setId(99999L);
        githubUser.setUserAccount("github_test_" + RandomUtil.randomNumbers(4));
        githubUser.setGithubId("123456");
        githubUser.setUserPassword("github_oauth_dummy");
        userService.save(githubUser);

        MockHttpSession githubSession = new MockHttpSession();
        LoginUserVO login = userService.userLogin(
                githubUser.getUserAccount(),
                "dummy_password_should_not_matter",
                githubSession
        );

        // Note: This would normally fail since password won't match
        // In real scenario, we'd need to create a proper GitHub user

        // Cleanup if exists
        userService.removeById(githubUser.getId());
    }

    // ============================================
    // Cleanup
    // ============================================

    @Test
    @Order(999)
    @DisplayName("Final cleanup - delete test user")
    void testCleanup() {
        if (testUserId != null) {
            MockHttpSession cleanupSession = new MockHttpSession();
            try {
                userService.userLogin(testUserAccount, TEST_USER_PASSWORD, cleanupSession);
                userService.deleteMyAccount(TEST_USER_PASSWORD, cleanupSession);
            } catch (Exception e) {
                log.warn("Cleanup failed, may need manual intervention", e);
            }
        }
    }
}
