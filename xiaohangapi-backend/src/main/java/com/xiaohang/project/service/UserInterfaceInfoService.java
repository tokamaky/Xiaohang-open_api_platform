package com.xiaohang.project.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohang.xiaohangapicommon.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.xiaohang.xiaohangapicommon.model.entity.UserInterfaceInfo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 14711
* @description 针对表【user_interface_info(User-API relationship)】的数据库操作Service
* @createDate 2024-12-20 19:01:24
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    /**
     * Validation
     *
     * @param userInterfaceInfo
     * @param add
     */
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * Get query conditions
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest interfaceInfoQueryRequest);

    /**
     * Get paginated interface information wrapper
     *
     * @param userInterfaceInfoPage
     * @param request
     * @return
     */
    Page<UserInterfaceInfo> getUserInterfaceInfoVOPage(Page<UserInterfaceInfo> userInterfaceInfoPage, HttpServletRequest request);

    /**
     * Get top n interface information based on call ranking
     *
     * @param limit Top n
     * @return List<UserInterfaceInfo>
     */
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}