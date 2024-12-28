package com.xiaohang.project.service.impl.inner;


import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaohang.project.common.ErrorCode;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.mapper.InterfaceInfoMapper;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.xiaohangapicommon.service.InnerInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
/**
 * 内部接口服务实现类
 *
 * @author xiaohang
 */
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Override
    public InterfaceInfo getInvokeInterfaceInfo(String url, String method) {
        if (StringUtils.isAnyBlank(url, method)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", url);
        queryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

}
