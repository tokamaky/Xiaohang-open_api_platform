package com.xiaohang.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohang.xiaohangapicommon.model.entity.UserInterfaceInfo;

/**
* @author 14711
* @description 针对表【user_interface_info(User-API relationship)】的数据库操作Service
* @createDate 2024-12-20 19:01:24
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * API Call Statistics
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
