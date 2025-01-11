package com.xiaohang.project.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaohang.xiaohangapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author 14711
* @description 针对表【user_interface_info(User-API relationship)】的数据库操作Mapper
* @createDate 2024-12-20 19:01:24
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(long currentUserid, int limit);
}




