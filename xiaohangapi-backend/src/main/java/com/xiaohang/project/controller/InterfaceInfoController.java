package com.xiaohang.project.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.xiaohang.project.annotation.AuthCheck;
import com.xiaohang.project.config.GatewayConfig;
import com.xiaohang.project.exception.*;
import com.xiaohang.xiaohangapicommon.common.*;
import com.xiaohang.xiaohangapicommon.constant.CommonConstant;
import com.xiaohang.xiaohangapicommon.constant.UserConstant;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.project.service.InterfaceInfoService;
import com.xiaohang.project.service.UserService;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapiclientsdk.Client.XiaohangApiClient;
import com.xiaohang.xiaohangapicommon.model.enums.InterfaceInfoStatusEnum;
import com.xiaohang.xiaohangapicommon.model.vo.InterfaceInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * InterfaceInfo API Controller
 *
 * Handles operations for managing interfaceInfos.
 *
 * Author: xiaohang</a>
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private GatewayConfig gatewayConfig;

    @Autowired
    private XiaohangApiClient xiaohangApiClient;

    // region CRUD Operations

    /**
     * Create a new interfaceInfo
     *
     * @param interfaceInfoAddRequest request containing interfaceInfo details
     * @param request                 HTTP request object
     * @return response containing new interfaceInfo ID
     */
    @PostMapping("/add")
    public BaseResponse<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);

        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        interfaceInfo.setRequestParamsRemark(JSONUtil.toJsonStr(interfaceInfoAddRequest.getRequestParamsRemark()));
        interfaceInfo.setResponseParamsRemark(JSONUtil.toJsonStr(interfaceInfoAddRequest.getResponseParamsRemark()));
        boolean result = interfaceInfoService.save(interfaceInfo);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newInterfaceInfoId = interfaceInfo.getId();
        return ResultUtils.success(newInterfaceInfoId);
    }


    /**
     * Delete a interfaceInfo
     *
     * @param deleteRequest request containing interfaceInfo ID to delete
     * @param request       HTTP request object
     * @return response indicating success or failure
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // Check if the deleteRequest is null or if the ID is invalid
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // Get the currently logged-in user
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();

        // Check if the interface information exists
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        // Only the user who created the interface or an admin can delete it
        if (!oldInterfaceInfo.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // Remove the interface info by ID and return the result
        boolean b = interfaceInfoService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * Update a interfaceInfo
     *
     * @param interfaceInfoUpdateRequest request containing updated interfaceInfo details
     * @return response indicating success or failure
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = interfaceInfoService.updateInterfaceInfo(interfaceInfoUpdateRequest);
        return ResultUtils.success(result);
    }

    /**
     * Get a interfaceInfo by ID
     *
     * @param id interfaceInfo ID
     * @return response containing interfaceInfo details
     */
    @GetMapping("/get/vo")
    public BaseResponse<InterfaceInfoVO> getInterfaceInfoVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVO(interfaceInfo, request));
    }

    /**
     * Paginated list retrieval (wrapper class)
     *
     * @param interfaceInfoQueryRequest Query conditions
     * @param request                   Request
     * @return Paginated list
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                         HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        // Descending order sorting
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // Limit web crawlers
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOPage(interfaceInfoPage, request));
    }

    /**
     * Paginated list retrieval by current user ID (wrapper class)
     *
     * @param interfaceInfoQueryRequest Query conditions
     * @param request                   Request
     * @return Paginated list
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<InterfaceInfoVO>> listInterfaceInfoVOByUserIdPage(@RequestBody InterfaceInfoQueryRequest interfaceInfoQueryRequest,
                                                                               HttpServletRequest request) {
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        interfaceInfoQueryRequest.setSortField("createTime");
        // Descending order sorting
        interfaceInfoQueryRequest.setSortOrder(CommonConstant.SORT_ORDER_DESC);
        // Limit web crawlers
        ThrowUtils.throwIf(size > 30, ErrorCode.PARAMS_ERROR);
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.page(new Page<>(current, size),
                interfaceInfoService.getQueryWrapper(interfaceInfoQueryRequest));
        return ResultUtils.success(interfaceInfoService.getInterfaceInfoVOByUserIdPage(interfaceInfoPage, request));
    }
// endregion

// region Publish and offline interface calls

    /**
     * Publish (admin only)
     *
     * @param interfaceInfoInvokeRequest Interface info
     * @return Success status
     */
    @PostMapping("/online")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> onlineInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                     HttpServletRequest request) {
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // Check if the interface exists
        Long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);
        // Check if it can be called
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();
        // Interface request URL
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        // Get SDK client
        xiaohangApiClient = interfaceInfoService.getXiaohangApiClient(request);
        // Set gateway address
        xiaohangApiClient.setGatewayHost(gatewayConfig.getHost());
        try {
            // Invoke the method
            String invokeResult = xiaohangApiClient.invokeInterface(requestParams, url, method);
            if (StringUtils.isBlank(invokeResult)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface data is empty");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface validation failed");
        }

        // Change interface status to online
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.ONLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * Offline (admin only)
     *
     * @param idRequest Interface ID
     * @return Success status
     */
    @PostMapping("/offline")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> offlineInterfaceInfo(@RequestBody IdRequest idRequest) {
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // Check if the interface exists
        Long id = idRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return ResultUtils.success(result);
    }

    /**
     * Test invocation
     *
     * @param interfaceInfoInvokeRequest Test invocation request
     * @return Success status
     */
    @PostMapping(value = "/invoke")
    public BaseResponse<Object> invokeInterfaceInfo(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest,
                                                    HttpServletRequest request) throws UnsupportedEncodingException {
        // Validate parameters and check if the interface exists
        if (interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = interfaceInfoInvokeRequest.getId();
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        ThrowUtils.throwIf(oldInterfaceInfo == null, ErrorCode.NOT_FOUND_ERROR);

        if (oldInterfaceInfo.getStatus().equals(InterfaceInfoStatusEnum.OFFLINE.getValue())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Interface is closed");
        }
        // Interface request URL
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        String requestParams = interfaceInfoInvokeRequest.getRequestParams();
        // Get SDK client
        xiaohangApiClient = interfaceInfoService.getXiaohangApiClient(request);
        // Set gateway address
        xiaohangApiClient.setGatewayHost(gatewayConfig.getHost());
        String invokeResult = null;
        try {
            // Invoke the method
            invokeResult = xiaohangApiClient.invokeInterface(requestParams, url, method);
            if (StringUtils.isBlank(invokeResult)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface data is empty");
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface validation failed");
        }
        return ResultUtils.success(invokeResult);
    }
}
// endregion
