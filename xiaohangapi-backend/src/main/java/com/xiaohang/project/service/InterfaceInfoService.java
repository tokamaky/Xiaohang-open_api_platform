package com.xiaohang.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohang.xiaohangapiclientsdk.Client.XiaohangApiClient;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.xiaohangapicommon.model.vo.InterfaceInfoVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import javax.servlet.http.HttpServletRequest;

/**
* @author 14711
* @description 针对表【interface_info(Interface Information)】的数据库操作Service
* @createDate 2024-12-12 10:01:16
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {

    /**
     * Validation
     *
     * @param interfaceInfo
     * @param add
     */
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    /**
     * Get query conditions
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<InterfaceInfo> getQueryWrapper(InterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * Get interface information wrapper
     *
     * @param interfaceInfo
     * @param request
     * @return
     */
    InterfaceInfoVO getInterfaceInfoVO(InterfaceInfo interfaceInfo, HttpServletRequest request);

    /**
     * Get paginated interface information wrapper
     *
     * @param interfaceInfoPage
     * @param request
     * @return
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);

    /**
     * Create SDK client
     *
     * @param request Current session
     * @return SDK client
     */
    XiaohangApiClient getXiaohangApiClient(HttpServletRequest request);

    /**
     * Update interface information
     *
     * @param interfaceInfoUpdateRequest Interface information update request
     * @return Whether the update was successful
     */
    boolean updateInterfaceInfo(InterfaceInfoUpdateRequest interfaceInfoUpdateRequest);

    /**
     * Get paginated interface information wrapper by user ID
     *
     * @param interfaceInfoPage Interface information pagination
     * @param request           Current session
     * @return Interface information pagination
     */
    Page<InterfaceInfoVO> getInterfaceInfoVOByUserIdPage(Page<InterfaceInfo> interfaceInfoPage, HttpServletRequest request);
}
