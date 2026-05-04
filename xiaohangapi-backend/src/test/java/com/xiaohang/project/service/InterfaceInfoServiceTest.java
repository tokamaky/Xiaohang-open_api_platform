package com.xiaohang.project.service;

import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.service.impl.InterfaceInfoServiceImpl;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for InterfaceInfoService
 * Tests business logic methods without database dependency
 */
@RunWith(MockitoJUnitRunner.class)
public class InterfaceInfoServiceTest {

    @InjectMocks
    private InterfaceInfoServiceImpl interfaceInfoService;

    @Mock
    private UserService userService;

    @Mock
    private UserInterfaceInfoService userInterfaceInfoService;

    /**
     * Test validInterfaceInfo with null interface
     */
    @Test(expected = BusinessException.class)
    public void testValidInterfaceInfo_NullInterface() {
        interfaceInfoService.validInterfaceInfo(null, true);
    }

    /**
     * Test validInterfaceInfo with empty required fields (add mode)
     */
    @Test
    public void testValidInterfaceInfo_EmptyFields_AddMode() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        try {
            interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * Test validInterfaceInfo with empty name only
     */
    @Test
    public void testValidInterfaceInfo_EmptyName() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setDescription("Test description");
        interfaceInfo.setUrl("http://test.com");
        interfaceInfo.setHost("test.com");
        interfaceInfo.setRequestParams("{}");
        interfaceInfo.setMethod("GET");

        try {
            interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
        }
    }

    /**
     * Test validInterfaceInfo with name too long
     */
    @Test
    public void testValidInterfaceInfo_NameTooLong() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        // Create a name that is more than 50 characters
        interfaceInfo.setName("a" + "b".repeat(50));
        interfaceInfo.setDescription("Test");
        interfaceInfo.setUrl("http://test.com");
        interfaceInfo.setHost("test.com");
        interfaceInfo.setRequestParams("{}");
        interfaceInfo.setMethod("GET");

        try {
            interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
            fail("Expected BusinessException");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.PARAMS_ERROR.getCode(), e.getCode());
            assertTrue(e.getMessage().contains("too long"));
        }
    }

    /**
     * Test validInterfaceInfo with valid interface
     */
    @Test
    public void testValidInterfaceInfo_ValidInterface() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setName("Test API");
        interfaceInfo.setDescription("Test description");
        interfaceInfo.setUrl("http://test.com/api");
        interfaceInfo.setHost("test.com");
        interfaceInfo.setRequestParams("{}");
        interfaceInfo.setMethod("GET");

        // Should not throw exception
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
    }

    /**
     * Test validInterfaceInfo in update mode (not adding) - name can be empty
     */
    @Test
    public void testValidInterfaceInfo_UpdateMode_EmptyName() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        // Empty name is allowed in update mode
        interfaceInfo.setDescription("Test description");

        // Should not throw exception
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
    }

    /**
     * Test getQueryWrapper with null request
     */
    @Test
    public void testGetQueryWrapper_NullRequest() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InterfaceInfo> wrapper =
            interfaceInfoService.getQueryWrapper(null);
        assertNotNull(wrapper);
    }

    /**
     * Test getQueryWrapper with name filter
     */
    @Test
    public void testGetQueryWrapper_WithName() {
        InterfaceInfoQueryRequest request = new InterfaceInfoQueryRequest();
        request.setName("testApi");

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InterfaceInfo> wrapper =
            interfaceInfoService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    /**
     * Test getQueryWrapper with method filter
     */
    @Test
    public void testGetQueryWrapper_WithMethod() {
        InterfaceInfoQueryRequest request = new InterfaceInfoQueryRequest();
        request.setMethod("GET");

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InterfaceInfo> wrapper =
            interfaceInfoService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    /**
     * Test getQueryWrapper with status filter
     */
    @Test
    public void testGetQueryWrapper_WithStatus() {
        InterfaceInfoQueryRequest request = new InterfaceInfoQueryRequest();
        request.setStatus(1);

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InterfaceInfo> wrapper =
            interfaceInfoService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    /**
     * Test getQueryWrapper with search text
     */
    @Test
    public void testGetQueryWrapper_WithSearchText() {
        InterfaceInfoQueryRequest request = new InterfaceInfoQueryRequest();
        request.setSearchText("test search");

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InterfaceInfo> wrapper =
            interfaceInfoService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    /**
     * Test getQueryWrapper with sort order
     */
    @Test
    public void testGetQueryWrapper_WithSortOrder() {
        InterfaceInfoQueryRequest request = new InterfaceInfoQueryRequest();
        request.setSortField("createTime");
        request.setSortOrder("asc");

        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<InterfaceInfo> wrapper =
            interfaceInfoService.getQueryWrapper(request);
        assertNotNull(wrapper);
    }

    /**
     * Test updateInterfaceInfo with null request
     */
    @Test(expected = NullPointerException.class)
    public void testUpdateInterfaceInfo_NullId() {
        InterfaceInfoUpdateRequest request = new InterfaceInfoUpdateRequest();
        request.setId(null);
        interfaceInfoService.updateInterfaceInfo(request);
    }

    /**
     * Test InterfaceInfo entity fields
     */
    @Test
    public void testInterfaceInfo_EntityFields() {
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(1L);
        interfaceInfo.setName("Test API");
        interfaceInfo.setDescription("Test description");
        interfaceInfo.setUrl("http://test.com/api");
        interfaceInfo.setHost("test.com");
        interfaceInfo.setRequestParams("{\"name\":\"value\"}");
        interfaceInfo.setRequestParamsRemark("test param");
        interfaceInfo.setResponseParamsRemark("response param");
        interfaceInfo.setMethod("POST");
        interfaceInfo.setUserId(1L);
        interfaceInfo.setStatus(1);
        interfaceInfo.setCreateTime(new Date());
        interfaceInfo.setUpdateTime(new Date());

        assertEquals(Long.valueOf(1L), interfaceInfo.getId());
        assertEquals("Test API", interfaceInfo.getName());
        assertEquals("Test description", interfaceInfo.getDescription());
        assertEquals("http://test.com/api", interfaceInfo.getUrl());
        assertEquals("test.com", interfaceInfo.getHost());
        assertEquals("POST", interfaceInfo.getMethod());
        assertEquals(Long.valueOf(1L), interfaceInfo.getUserId());
        assertEquals(Integer.valueOf(1), interfaceInfo.getStatus());
    }
}
