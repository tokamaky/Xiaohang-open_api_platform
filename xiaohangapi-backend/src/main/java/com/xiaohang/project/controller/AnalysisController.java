package com.xiaohang.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaohang.project.annotation.AuthCheck;

import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.mapper.UserInterfaceInfoMapper;
import com.xiaohang.project.service.InterfaceInfoService;
import com.xiaohang.project.service.UserInterfaceInfoService;
import com.xiaohang.project.service.UserService;
import com.xiaohang.xiaohangapicommon.common.BaseResponse;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.common.ResultUtils;
import com.xiaohang.xiaohangapicommon.constant.UserConstant;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import com.xiaohang.xiaohangapicommon.model.entity.UserInterfaceInfo;
import com.xiaohang.xiaohangapicommon.model.vo.InterfaceInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分析控制器
 *
 * @author xiaohang
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo(HttpServletRequest request) {
        // 查询调用次数前3名的接口
        long currentUserId = userService.getLoginUser(request).getId();
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoService.listTopInvokeInterfaceInfo(currentUserId, 3);
        if (userInterfaceInfoList.isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Interface information does not exist");
        }
        // 根据接口id分组
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        // 查询所有接口id的接口信息
        List<InterfaceInfo> list = interfaceInfoService.lambdaQuery()
                .in(InterfaceInfo::getId, interfaceInfoIdObjMap.keySet())
                .eq(InterfaceInfo::getIsDelete, 0)
                .list();
        if (list.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Interface information does not exist");
        }
        // 组装返回结果
        List<InterfaceInfoVO> result = list.stream().map(interfaceInfo -> {
            InterfaceInfoVO interfaceInfoVO = InterfaceInfoVO.objToVo(interfaceInfo);
            interfaceInfoVO.setTotalNum(interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum());
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return ResultUtils.success(result);
    }
}
