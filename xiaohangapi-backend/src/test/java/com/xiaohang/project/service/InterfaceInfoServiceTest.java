package com.xiaohang.project.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.vo.InterfaceInfoVO;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.common.IdRequest;
import com.xiaohang.xiaohangapicommon.model.enums.InterfaceInfoStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Interface Info Service Tests
 * Tests for interface CRUD operations and management
 *
 * @author xiaohang
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public class InterfaceInfoServiceTest {

    @Autowired
    private InterfaceInfoService interfaceInfoService;

    @Autowired
    private UserService userService;

    private static final String TEST_USER_ACCOUNT = "apitest_" + System.currentTimeMillis();
    private static final String TEST_PASSWORD = "password123";
    private static Long testUserId;
    private static Long testInterfaceId;
    private static MockHttpSession testSession;

    @BeforeAll
    static void setup(@Autowired InterfaceInfoService interfaceInfoService,
                      @Autowired UserService userService) {
        // Create test user
        try {
            testUserId = userService.userRegister(TEST_USER_ACCOUNT, TEST_PASSWORD, TEST_PASSWORD);
            log.info("Created test user with ID: {}", testUserId);
        } catch (Exception e) {
            log.warn("Test user might already exist: {}", e.getMessage());
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("userAccount", TEST_USER_ACCOUNT);
            User existingUser = userService.getOne(wrapper);
            if (existingUser != null) {
                testUserId = existingUser.getId();
            }
        }
    }

    @BeforeEach
    void setupSession() {
        testSession = new MockHttpSession();
        userService.userLogin(TEST_USER_ACCOUNT, TEST_PASSWORD, testSession);
    }

    @AfterAll
    static void cleanup() {
        if (testUserId != null && testInterfaceId != null) {
            try {
                interfaceInfoService.removeById(testInterfaceId);
            } catch (Exception ignored) {
            }
        }
        if (testUserId != null) {
            try {
                userService.removeById(testUserId);
            } catch (Exception ignored) {
            }
        }
    }

    // ============================================
    // Create Interface Tests
    // ============================================

    @Test
    @Order(1)
    @DisplayName("Test add interface successfully")
    void testAddInterface_Success() {
        InterfaceInfoAddRequest request = new InterfaceInfoAddRequest();
        request.setName("Test API");
        request.setDescription("Test API Description");
        request.setUrl("https://api.test.com/test");
        request.setMethod("GET");
        request.setRequestParams("[{\"name\":\"param1\",\"type\":\"string\",\"required\":true}]");
        request.setResponseParams("[{\"name\":\"result\",\"type\":\"object\"}]");
        request.setRequestParamsRemark(JSONUtil.createArray().toString());
        request.setResponseParamsRemark(JSONUtil.createArray().toString());

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setName(request.getName());
        interfaceInfo.setDescription(request.getDescription());
        interfaceInfo.setUrl(request.getUrl());
        interfaceInfo.setMethod(request.getMethod());
        interfaceInfo.setRequestParams(request.getRequestParams());
        interfaceInfo.setResponseParams(request.getResponseParams());
        interfaceInfo.setUserId(testUserId);

        boolean result = interfaceInfoService.save(interfaceInfo);
        assertTrue(result, "Interface should be saved successfully");
        assertNotNull(interfaceInfo.getId(), "Interface ID should be generated");
        testInterfaceId = interfaceInfo.getId();
        log.info("Created interface with ID: {}", interfaceInfo.getId());
    }

    @Test
    @Order(2)
    @DisplayName("Test add interface with invalid data")
    void testAddInterface_InvalidData() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setName("");

        assertThrows(BusinessException.class,
                () -> interfaceInfoService.validInterfaceInfo(interfaceInfo, true));

        interfaceInfo.setName("Valid Name");
        interfaceInfo.setUrl("");
        assertThrows(BusinessException.class,
                () -> interfaceInfoService.validInterfaceInfo(interfaceInfo, true));
    }

    // ============================================
    // Query Interface Tests
    // ============================================

    @Test
    @Order(10)
    @DisplayName("Test get interface by ID")
    void testGetInterfaceById() {
        if (testInterfaceId == null) {
            log.warn("Skipping test - no test interface created");
            return;
        }

        InterfaceInfo interfaceInfo = interfaceInfoService.getById(testInterfaceId);
        assertNotNull(interfaceInfo, "Interface should exist");
        assertEquals("Test API", interfaceInfo.getName());
    }

    @Test
    @Order(11)
    @DisplayName("Test get interface VO")
    void testGetInterfaceInfoVO() {
        if (testInterfaceId == null) {
            log.warn("Skipping test - no test interface created");
            return;
        }

        InterfaceInfo interfaceInfo = interfaceInfoService.getById(testInterfaceId);
        InterfaceInfoVO vo = interfaceInfoService.getInterfaceInfoVO(interfaceInfo, testSession);

        assertNotNull(vo, "VO should not be null");
        assertEquals(interfaceInfo.getId(), vo.getId());
        assertEquals(interfaceInfo.getName(), vo.getName());
    }

    @Test
    @Order(12)
    @DisplayName("Test list interface VO by page")
    void testListInterfaceInfoVOByPage() {
        InterfaceInfoQueryRequest queryRequest = new InterfaceInfoQueryRequest();
        queryRequest.setCurrent(1);
        queryRequest.setPageSize(10);

        Page<InterfaceInfo> page = new Page<>(1, 10);
        QueryWrapper<InterfaceInfo> wrapper = interfaceInfoService.getQueryWrapper(queryRequest);
        Page<InterfaceInfo> resultPage = interfaceInfoService.page(page, wrapper);

        assertNotNull(resultPage, "Result page should not be null");
        assertTrue(resultPage.getTotal() >= 0, "Total should be >= 0");
    }

    // ============================================
    // Update Interface Tests
    // ============================================

    @Test
    @Order(20)
    @DisplayName("Test update interface info")
    void testUpdateInterfaceInfo() {
        if (testInterfaceId == null) {
            log.warn("Skipping test - no test interface created");
            return;
        }

        InterfaceInfoUpdateRequest updateRequest = new InterfaceInfoUpdateRequest();
        updateRequest.setId(testInterfaceId);
        updateRequest.setName("Updated Test API");
        updateRequest.setDescription("Updated Description");

        boolean result = interfaceInfoService.updateInterfaceInfo(updateRequest);
        assertTrue(result, "Update should succeed");

        InterfaceInfo updated = interfaceInfoService.getById(testInterfaceId);
        assertEquals("Updated Test API", updated.getName());
        assertEquals("Updated Description", updated.getDescription());
    }

    // ============================================
    // Delete Interface Tests
    // ============================================

    @Test
    @Order(30)
    @DisplayName("Test delete interface")
    void testDeleteInterface() {
        // Create a temporary interface to delete
        InterfaceInfo tempInterface = new InterfaceInfo();
        tempInterface.setName("Temp Interface");
        tempInterface.setDescription("To be deleted");
        tempInterface.setUrl("https://api.test.com/temp");
        tempInterface.setMethod("POST");
        tempInterface.setUserId(testUserId);
        tempInterface.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());

        interfaceInfoService.save(tempInterface);
        Long tempId = tempInterface.getId();

        // Delete
        boolean result = interfaceInfoService.removeById(tempId);
        assertTrue(result, "Delete should succeed");

        // Verify deleted
        InterfaceInfo deleted = interfaceInfoService.getById(tempId);
        assertNull(deleted, "Interface should not exist after deletion");
    }

    // ============================================
    // Validation Tests
    // ============================================

    @Test
    @Order(40)
    @DisplayName("Test interface validation - null name")
    void testValidInterfaceInfo_NullName() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setUrl("https://api.test.com");
        interfaceInfo.setMethod("GET");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> interfaceInfoService.validInterfaceInfo(interfaceInfo, true));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @Order(41)
    @DisplayName("Test interface validation - null URL")
    void testValidInterfaceInfo_NullUrl() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setName("Valid Name");
        interfaceInfo.setMethod("GET");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> interfaceInfoService.validInterfaceInfo(interfaceInfo, true));
        assertEquals(ErrorCode.PARAMS_ERROR.getCode(), exception.getCode());
    }

    @Test
    @Order(42)
    @DisplayName("Test interface validation - invalid method")
    void testValidInterfaceInfo_InvalidMethod() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setName("Valid Name");
        interfaceInfo.setUrl("https://api.test.com");
        interfaceInfo.setMethod("INVALID");

        assertThrows(BusinessException.class,
                () -> interfaceInfoService.validInterfaceInfo(interfaceInfo, true));
    }

    @Test
    @Order(43)
    @DisplayName("Test interface validation - valid interface")
    void testValidInterfaceInfo_Valid() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setName("Valid Name");
        interfaceInfo.setUrl("https://api.test.com");
        interfaceInfo.setMethod("GET");

        assertDoesNotThrow(() -> interfaceInfoService.validInterfaceInfo(interfaceInfo, true));
    }

    // ============================================
    // Query Wrapper Tests
    // ============================================

    @Test
    @Order(50)
    @DisplayName("Test get query wrapper")
    void testGetQueryWrapper() {
        InterfaceInfoQueryRequest request = new InterfaceInfoQueryRequest();
        request.setName("Test");
        request.setMethod("GET");
        request.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());

        QueryWrapper<InterfaceInfo> wrapper = interfaceInfoService.getQueryWrapper(request);
        assertNotNull(wrapper, "Query wrapper should not be null");
    }

    // ============================================
    // Pagination Tests
    // ============================================

    @Test
    @Order(60)
    @DisplayName("Test interface info VO page")
    void testGetInterfaceInfoVOPage() {
        InterfaceInfoQueryRequest queryRequest = new InterfaceInfoQueryRequest();
        queryRequest.setCurrent(1);
        queryRequest.setPageSize(10);

        Page<InterfaceInfo> page = new Page<>(1, 10);
        QueryWrapper<InterfaceInfo> wrapper = interfaceInfoService.getQueryWrapper(queryRequest);
        Page<InterfaceInfo> interfacePage = interfaceInfoService.page(page, wrapper);

        Page<InterfaceInfoVO> voPage = interfaceInfoService.getInterfaceInfoVOPage(interfacePage, testSession);
        assertNotNull(voPage, "VO page should not be null");
        assertEquals(interfacePage.getTotal(), voPage.getTotal());
    }
}
