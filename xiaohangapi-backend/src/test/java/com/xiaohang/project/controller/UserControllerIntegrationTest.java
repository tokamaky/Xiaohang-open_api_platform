package com.xiaohang.project.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaohang.xiaohangapicommon.model.dto.user.UserRegisterRequest;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.project.service.UserService;
import com.xiaohang.xiaohangapiclientsdk.Client.XiaohangApiClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for User Controller
 * Tests all user-related API endpoints
 *
 * @author xiaohang
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    private static MockHttpSession testSession;
    private static String testAccount;
    private static final String TEST_PASSWORD = "password123";
    private static Long testUserId;

    @MockBean
    private XiaohangApiClient xiaohangApiClient;

    @BeforeAll
    void setup() {
        testAccount = "testcontroller_" + System.currentTimeMillis();
    }

    @AfterAll
    void cleanup() {
        if (testUserId != null) {
            try {
                userService.removeById(testUserId);
            } catch (Exception ignored) {
            }
        }
    }

    // ============================================
    // User Registration Tests
    // ============================================

    @Test
    @Order(1)
    @DisplayName("POST /user/register - Success")
    void testUserRegister_Success() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount(testAccount);
        registerRequest.setUserPassword(TEST_PASSWORD);
        registerRequest.setCheckPassword(TEST_PASSWORD);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").isNumber())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();
                    assertTrue(response.contains("\"code\":0"));
                    testUserId = Long.parseLong(
                            response.replaceAll(".*\"data\":(\\d+).*", "$1")
                    );
                });
    }

    @Test
    @Order(2)
    @DisplayName("POST /user/register - Duplicate Account")
    void testUserRegister_DuplicateAccount() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount(testAccount);
        registerRequest.setUserPassword(TEST_PASSWORD);
        registerRequest.setCheckPassword(TEST_PASSWORD);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(registerRequest)))
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @Order(3)
    @DisplayName("POST /user/register - Invalid Parameters")
    void testUserRegister_InvalidParams() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount("");
        registerRequest.setUserPassword(TEST_PASSWORD);
        registerRequest.setCheckPassword(TEST_PASSWORD);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(registerRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(4)
    @DisplayName("POST /user/register - Password Mismatch")
    void testUserRegister_PasswordMismatch() throws Exception {
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount("newaccount_" + System.currentTimeMillis());
        registerRequest.setUserPassword(TEST_PASSWORD);
        registerRequest.setCheckPassword("different_password");

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(registerRequest)))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // ============================================
    // User Login Tests
    // ============================================

    @Test
    @Order(10)
    @DisplayName("POST /user/login - Success")
    void testUserLogin_Success() throws Exception {
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value(testAccount))
                .andExpect(jsonPath("$.data.accessKey").exists())
                .andExpect(jsonPath("$.data.secretKey").exists());
    }

    @Test
    @Order(11)
    @DisplayName("POST /user/login - Wrong Password")
    void testUserLogin_WrongPassword() throws Exception {
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", "wrongpassword")
                .toString();

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(jsonPath("$.code").value(40000));
    }

    @Test
    @Order(12)
    @DisplayName("POST /user/login - Non-existent User")
    void testUserLogin_NonExistent() throws Exception {
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", "nonexistent123456")
                .set("userPassword", TEST_PASSWORD)
                .toString();

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // ============================================
    // User Logout Tests
    // ============================================

    @Test
    @Order(20)
    @DisplayName("POST /user/logout - Success")
    void testUserLogout_Success() throws Exception {
        // First login to get a valid session
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(post("/user/logout")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @Order(21)
    @DisplayName("POST /user/logout - Without Login")
    void testUserLogout_NotLoggedIn() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();

        mockMvc.perform(post("/user/logout")
                        .session(emptySession))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // ============================================
    // Get Login User Tests
    // ============================================

    @Test
    @Order(30)
    @DisplayName("GET /user/get/login - Success")
    void testGetLoginUser_Success() throws Exception {
        // Login first
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        mockMvc.perform(get("/user/get/login")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userAccount").value(testAccount));
    }

    @Test
    @Order(31)
    @DisplayName("GET /user/get/login - Not Logged In")
    void testGetLoginUser_NotLoggedIn() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();

        mockMvc.perform(get("/user/get/login")
                        .session(emptySession))
                .andExpect(jsonPath("$.code").value(40100));
    }

    // ============================================
    // User CRUD Operations (Admin Required)
    // ============================================

    @Test
    @Order(40)
    @DisplayName("POST /user/add - Admin Only")
    void testAddUser_RequiresAdmin() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();

        String addRequest = JSONUtil.createObj()
                .set("userAccount", "newuser_" + System.currentTimeMillis())
                .set("userPassword", TEST_PASSWORD)
                .set("userName", "New User")
                .toString();

        mockMvc.perform(post("/user/add")
                        .session(emptySession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addRequest))
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @Order(41)
    @DisplayName("POST /user/delete - Admin Only")
    void testDeleteUser_RequiresAdmin() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();

        String deleteRequest = JSONUtil.createObj()
                .set("id", 1)
                .toString();

        mockMvc.perform(post("/user/delete")
                        .session(emptySession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deleteRequest))
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @Order(42)
    @DisplayName("POST /user/list/page - Admin Only")
    void testListUserByPage_RequiresAdmin() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();

        String listRequest = JSONUtil.createObj()
                .set("current", 1)
                .set("pageSize", 10)
                .toString();

        mockMvc.perform(post("/user/list/page")
                        .session(emptySession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(listRequest))
                .andExpect(jsonPath("$.code").value(40100));
    }

    @Test
    @Order(43)
    @DisplayName("GET /user/get - Admin Only")
    void testGetUserById_RequiresAdmin() throws Exception {
        MockHttpSession emptySession = new MockHttpSession();

        mockMvc.perform(get("/user/get")
                        .param("id", "1")
                        .session(emptySession))
                .andExpect(jsonPath("$.code").value(40100));
    }

    // ============================================
    // Update User Tests
    // ============================================

    @Test
    @Order(50)
    @DisplayName("POST /user/update - Success")
    void testUpdateUser_Success() throws Exception {
        // Login first
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        String updateRequest = JSONUtil.createObj()
                .set("id", testUserId)
                .set("userName", "Updated Name")
                .toString();

        mockMvc.perform(post("/user/update")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    // ============================================
    // Password Change Tests
    // ============================================

    @Test
    @Order(60)
    @DisplayName("POST /user/change/password - Success")
    void testChangePassword_Success() throws Exception {
        // Login first
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        String changeRequest = JSONUtil.createObj()
                .set("userPassword", TEST_PASSWORD)
                .set("newPassword", "newpassword123")
                .toString();

        mockMvc.perform(post("/user/change/password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // Change back to original password for other tests
        String revertRequest = JSONUtil.createObj()
                .set("userPassword", "newpassword123")
                .set("newPassword", TEST_PASSWORD)
                .toString();

        mockMvc.perform(post("/user/change/password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(revertRequest))
                .andExpect(status().isOk());
    }

    @Test
    @Order(61)
    @DisplayName("POST /user/change/password - Wrong Old Password")
    void testChangePassword_WrongOldPassword() throws Exception {
        // Login first
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", testAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        String changeRequest = JSONUtil.createObj()
                .set("userPassword", "wrongpassword")
                .set("newPassword", "newpassword123")
                .toString();

        mockMvc.perform(post("/user/change/password")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeRequest))
                .andExpect(jsonPath("$.code").value(40000));
    }

    // ============================================
    // Account Deletion Tests
    // ============================================

    @Test
    @Order(70)
    @DisplayName("POST /user/delete/account - Success")
    void testDeleteMyAccount_Success() throws Exception {
        // Create a temporary account for deletion test
        String deleteTestAccount = "deletetest_" + System.currentTimeMillis();
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUserAccount(deleteTestAccount);
        registerRequest.setUserPassword(TEST_PASSWORD);
        registerRequest.setCheckPassword(TEST_PASSWORD);

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtil.toJsonStr(registerRequest)))
                .andExpect(jsonPath("$.code").value(0));

        // Login
        String loginRequest = JSONUtil.createObj()
                .set("userAccount", deleteTestAccount)
                .set("userPassword", TEST_PASSWORD)
                .toString();

        MvcResult loginResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession();

        // Delete account
        String deleteRequest = JSONUtil.createObj()
                .set("userPassword", TEST_PASSWORD)
                .toString();

        mockMvc.perform(post("/user/delete/account")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deleteRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }
}
