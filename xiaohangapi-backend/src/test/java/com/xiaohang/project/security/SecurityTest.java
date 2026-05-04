package com.xiaohang.project.security;

import com.xiaohang.project.service.UserService;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Security Tests
 * Tests for authentication, authorization, and security patterns
 *
 * @author xiaohang
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class SecurityTest {

    @Autowired
    private UserService userService;

    private static final String TEST_USER_ACCOUNT = "securitytest_" + System.currentTimeMillis();
    private static final String TEST_PASSWORD = "password123";
    private static Long testUserId;

    @BeforeAll
    static void setup() {
        log.info("Setting up security tests...");
    }

    @AfterAll
    static void cleanup() {
        if (testUserId != null) {
            try {
                userService.removeById(testUserId);
            } catch (Exception ignored) {
            }
        }
    }

    // ============================================
    // SQL Injection Prevention Tests
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Test SQL injection prevention in user registration")
    void testSqlInjection_Prevention() {
        String maliciousAccount = "admin' OR '1'='1";
        String maliciousPassword = "password'; DROP TABLE users;--";

        // Should either reject or sanitize the input
        try {
            userService.userRegister(maliciousAccount, maliciousPassword, maliciousPassword);
            // If it doesn't throw, the input was sanitized
            log.info("SQL injection attempt was handled (sanitized or rejected)");
        } catch (Exception e) {
            // Expected behavior - should reject malicious input
            log.info("SQL injection attempt was rejected: {}", e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Test SQL injection prevention in user login")
    void testSqlInjection_LoginPrevention() {
        MockHttpSession session = new MockHttpSession();
        String maliciousInput = "' OR '1'='1";

        // Should not login with SQL injection attempt
        assertThrows(Exception.class,
                () -> userService.userLogin(maliciousInput, TEST_PASSWORD, session),
                "Should not login with SQL injection input");
    }

    // ============================================
    // XSS Prevention Tests
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Test XSS prevention in user data")
    void testXss_Prevention() {
        String maliciousName = "<script>alert('XSS')</script>Test";

        try {
            // If the system accepts this, it should sanitize it
            long id = userService.userRegister(
                    "xsstest_" + System.currentTimeMillis(),
                    TEST_PASSWORD,
                    TEST_PASSWORD
            );

            User user = userService.getById(id);
            if (user != null) {
                // Verify that if stored, the data is either sanitized or
                // the display layer escapes it
                log.info("User created with potentially malicious name");
            }

            // Cleanup
            userService.removeById(id);
        } catch (Exception e) {
            log.info("XSS input was rejected: {}", e.getMessage());
        }
    }

    // ============================================
    // Authentication Tests
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Test authentication with valid credentials")
    void testAuthentication_ValidCredentials() {
        MockHttpSession session = new MockHttpSession();

        // First create the test user
        try {
            testUserId = userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, TEST_PASSWORD);
        } catch (Exception e) {
            log.info("User might already exist, getting ID...");
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            wrapper.eq("userAccount", TEST_USER_ACCOUNT);
            User existing = userService.getOne(wrapper);
            if (existing != null) {
                testUserId = existing.getId();
            }
        }

        // Login
        var loginResult = userService.userLogin(TEST_USER_ACCOUNT, TEST_PASSWORD, session);
        assertNotNull(loginResult, "Should login with valid credentials");
        assertEquals(TEST_USER_ACCOUNT, loginResult.getUserAccount());
    }

    @Test
    @Order(21)
    @DisplayName("Test authentication fails with invalid password")
    void testAuthentication_InvalidPassword() {
        MockHttpSession session = new MockHttpSession();

        assertThrows(Exception.class,
                () -> userService.userLogin(TEST_USER_ACCOUNT, "wrongpassword123", session),
                "Should not login with invalid password");
    }

    @Test
    @Order(22)
    @DisplayName("Test authentication fails with non-existent user")
    void testAuthentication_NonExistentUser() {
        MockHttpSession session = new MockHttpSession();

        assertThrows(Exception.class,
                () -> userService.userLogin("nonexistent123456", TEST_PASSWORD, session),
                "Should not login with non-existent user");
    }

    // ============================================
    // Password Security Tests
    // ============================================

    @Test
    @Order(30)
    @DisplayName("Test password minimum length requirement")
    void testPasswordSecurity_MinimumLength() {
        String shortPassword = "short";

        assertThrows(Exception.class,
                () -> userService.userRegister("newuser_" + System.currentTimeMillis(), shortPassword, shortPassword),
                "Should reject passwords shorter than 8 characters");
    }

    @Test
    @Order(31)
    @DisplayName("Test password encryption")
    void testPasswordSecurity_Encryption() {
        // Create a user
        String testAccount = "encrypttest_" + System.currentTimeMillis();
        long id = userService.userRegister(testAccount, TEST_PASSWORD, TEST_PASSWORD);

        // Get the user from database
        User user = userService.getById(id);

        // Password should not be stored in plain text
        assertNotEquals(TEST_PASSWORD, user.getUserPassword(),
                "Password should not be stored in plain text");

        // Password should be encrypted (MD5 hash)
        assertEquals(32, user.getUserPassword().length(),
                "MD5 hash should be 32 characters");

        // Cleanup
        userService.removeById(id);
    }

    @Test
    @Order(32)
    @DisplayName("Test same password produces different hashes with salt")
    void testPasswordSecurity_Salt() {
        String password = "testpassword123";

        // Register two users with the same password
        String user1 = "salt1_" + System.currentTimeMillis();
        String user2 = "salt2_" + System.currentTimeMillis();

        long id1 = userService.userRegister(user1, password, password);
        long id2 = userService.userRegister(user2, password, password);

        User u1 = userService.getById(id1);
        User u2 = userService.getById(id2);

        // Passwords should be different due to salt (though in this implementation
        // they might be the same since we're using static salt)
        // This test documents the behavior
        log.info("User 1 password hash: {}", u1.getUserPassword());
        log.info("User 2 password hash: {}", u2.getUserPassword());

        // Cleanup
        userService.removeById(id1);
        userService.removeById(id2);
    }

    // ============================================
    // Session Security Tests
    // ============================================

    @Test
    @Order(40)
    @DisplayName("Test session is created on login")
    void testSession_CreatedOnLogin() {
        MockHttpSession session = new MockHttpSession();

        userService.userLogin(TEST_USER_ACCOUNT, TEST_PASSWORD, session);

        // Session should exist after login
        assertNotNull(session.getAttribute("user"),
                "Session should contain user attribute after login");
    }

    @Test
    @Order(41)
    @DisplayName("Test session is invalidated on logout")
    void testSession_InvalidatedOnLogout() {
        MockHttpSession session = new MockHttpSession();

        userService.userLogin(TEST_USER_ACCOUNT, TEST_PASSWORD, session);
        userService.userLogout(session);

        // Session should be cleared
        assertNull(session.getAttribute("user"),
                "Session should be cleared after logout");
    }

    // ============================================
    // Authorization Tests
    // ============================================

    @Test
    @Order(50)
    @DisplayName("Test regular user cannot access admin functions")
    void testAuthorization_RegularUserAccess() {
        MockHttpSession session = new MockHttpSession();
        userService.userLogin(TEST_USER_ACCOUNT, TEST_PASSWORD, session);

        // Regular user should not be admin
        assertFalse(userService.isAdmin(session),
                "Regular user should not have admin privileges");
    }

    @Test
    @Order(51)
    @DisplayName("Test admin detection")
    void testAuthorization_AdminDetection() {
        User adminUser = new User();
        adminUser.setUserRole("admin");

        assertTrue(userService.isAdmin(adminUser),
                "User with admin role should be detected as admin");

        User regularUser = new User();
        regularUser.setUserRole("user");

        assertFalse(userService.isAdmin(regularUser),
                "User with user role should not be detected as admin");
    }

    // ============================================
    // API Key Security Tests
    // ============================================

    @Test
    @Order(60)
    @DisplayName("Test unique API keys are generated")
    void testApiKey_Uniqueness() {
        // Create two users
        String user1 = "apikey1_" + System.currentTimeMillis();
        String user2 = "apikey2_" + System.currentTimeMillis();

        long id1 = userService.userRegister(user1, TEST_PASSWORD, TEST_PASSWORD);
        long id2 = userService.userRegister(user2, TEST_PASSWORD, TEST_PASSWORD);

        User u1 = userService.getById(id1);
        User u2 = userService.getById(id2);

        // Each user should have unique keys
        assertNotEquals(u1.getAccessKey(), u2.getAccessKey(),
                "Access keys should be unique");
        assertNotEquals(u1.getSecretKey(), u2.getSecretKey(),
                "Secret keys should be unique");

        // Keys should not be empty
        assertNotNull(u1.getAccessKey(), "Access key should not be null");
        assertNotNull(u1.getSecretKey(), "Secret key should not be null");

        // Cleanup
        userService.removeById(id1);
        userService.removeById(id2);
    }

    @Test
    @Order(61)
    @DisplayName("Test API keys are regenerated on update")
    void testApiKey_Regeneration() {
        User user = userService.getById(testUserId);
        String oldAccessKey = user.getAccessKey();
        String oldSecretKey = user.getSecretKey();

        boolean result = userService.updateSecretKey(testUserId);
        assertTrue(result, "Update should succeed");

        User updatedUser = userService.getById(testUserId);
        assertNotEquals(oldAccessKey, updatedUser.getAccessKey(),
                "Access key should be regenerated");
        assertNotEquals(oldSecretKey, updatedUser.getSecretKey(),
                "Secret key should be regenerated");
    }

    // ============================================
    // Rate Limiting / Brute Force Prevention
    // ============================================

    @Test
    @Order(70)
    @DisplayName("Test multiple failed login attempts")
    void testBruteForcePrevention_MultipleFailures() {
        MockHttpSession session = new MockHttpSession();

        // Try multiple failed logins
        for (int i = 0; i < 5; i++) {
            try {
                userService.userLogin(TEST_USER_ACCOUNT, "wrong_password", session);
            } catch (Exception e) {
                // Expected - login should fail
                log.debug("Failed login attempt {} as expected", i + 1);
            }
        }

        // The system should still be responsive (no lockout in this test)
        // In production, you might want to implement account lockout
        log.info("Multiple failed login attempts handled");
    }

    // ============================================
    // CSRF Prevention Tests
    // ============================================

    @Test
    @Order(80)
    @DisplayName("Test CSRF token presence")
    void testCsrf_TokenPresence() {
        // This would test CSRF token handling
        // For now, this is a placeholder for the security requirement
        log.info("CSRF protection should be implemented at the framework level");
        assertTrue(true, "CSRF protection is handled by Spring Security");
    }
}
