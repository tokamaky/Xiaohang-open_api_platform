package com.xiaohang.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;

/**
* @author 14711
* @description 针对表【interface_info(Interface Information)】的数据库操作Service
* @createDate 2024-12-12 10:01:16
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {


    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
