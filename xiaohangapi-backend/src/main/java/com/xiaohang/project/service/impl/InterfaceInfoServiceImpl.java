package com.xiaohang.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohang.project.common.ErrorCode;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.model.entity.InterfaceInfo;
import generator.mapper.InterfaceInfoMapper;
import generator.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
* @author 14711
* @description 针对表【interface_info(Interface Information)】的数据库操作Service实现
* @createDate 2024-12-12 10:01:16
*/
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
    implements InterfaceInfoService{

    //@Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add){
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        // During creation, all parameters must be non-null
        if (add) {
            if (StringUtils.isAnyBlank(name)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "name too long ");
        }
    }

}




