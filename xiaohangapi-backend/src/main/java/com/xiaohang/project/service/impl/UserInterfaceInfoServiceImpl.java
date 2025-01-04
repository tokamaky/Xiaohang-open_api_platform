package com.xiaohang.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.exception.ThrowUtils;
import com.xiaohang.project.utils.SqlUtils;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.constant.CommonConstant;
import com.xiaohang.xiaohangapicommon.model.dto.userinterfaceinfo.UserInterfaceInfoQueryRequest;
import com.xiaohang.xiaohangapicommon.model.entity.UserInterfaceInfo;
import com.xiaohang.project.service.UserInterfaceInfoService;
import com.xiaohang.project.mapper.UserInterfaceInfoMapper;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 14711
* @description 针对表【user_interface_info(User-API relationship)】的数据库操作Service实现
* @createDate 2024-12-20 19:01:24
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{



        @Resource
        private UserInterfaceInfoMapper userInterfaceInfoMapper;

        @Override
        public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
            if (userInterfaceInfo == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            Long userId = userInterfaceInfo.getUserId();
            Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
            Integer totalNum = userInterfaceInfo.getTotalNum();
            Integer leftNum = userInterfaceInfo.getLeftNum();

            List<UserInterfaceInfo> list = this.lambdaQuery()
                    .eq(UserInterfaceInfo::getUserId, userId)
                    .eq(UserInterfaceInfo::getInterfaceInfoId, interfaceInfoId)
                    .list();
            if (!list.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "The user already has access to this interface");
            }

            // When adding, parameters cannot be empty
            if (add) {
                ThrowUtils.throwIf(userId == null || interfaceInfoId == null, ErrorCode.PARAMS_ERROR);
            }
        }

        /**
         * Get the query wrapper
         *
         * @param interfaceInfoQueryRequest
         * @return
         */
        @Override
        public QueryWrapper<UserInterfaceInfo> getQueryWrapper(UserInterfaceInfoQueryRequest interfaceInfoQueryRequest) {

            QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
            if (interfaceInfoQueryRequest == null) {
                return queryWrapper;
            }

            String searchText = interfaceInfoQueryRequest.getSearchText();
            Long id = interfaceInfoQueryRequest.getId();
            Long userId = interfaceInfoQueryRequest.getUserId();
            Long interfaceInfoId = interfaceInfoQueryRequest.getInterfaceInfoId();
            Integer totalNum = interfaceInfoQueryRequest.getTotalNum();
            Integer leftNum = interfaceInfoQueryRequest.getLeftNum();
            Integer status = interfaceInfoQueryRequest.getStatus();
            String sortField = interfaceInfoQueryRequest.getSortField();
            String sortOrder = interfaceInfoQueryRequest.getSortOrder();

            // Construct query conditions
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.like("name", searchText);
            }
            queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
            queryWrapper.eq(ObjectUtils.isNotEmpty(totalNum), "totalNum", totalNum);
            queryWrapper.eq(ObjectUtils.isNotEmpty(leftNum), "leftNum", leftNum);
            queryWrapper.eq(ObjectUtils.isNotEmpty(interfaceInfoId), "interfaceInfoId", interfaceInfoId);
            queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
            queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
            queryWrapper.eq("isDelete", false);
            queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                    sortField);
            return queryWrapper;
        }

        @Override
        public Page<UserInterfaceInfo> getUserInterfaceInfoVOPage(Page<UserInterfaceInfo> userInterfaceInfoPage, HttpServletRequest request) {
            List<UserInterfaceInfo> interfaceInfoList = userInterfaceInfoPage.getRecords();
            Page<UserInterfaceInfo> interfaceInfoVOPage = new Page<>(userInterfaceInfoPage.getCurrent(), userInterfaceInfoPage.getSize(), userInterfaceInfoPage.getTotal());
            if (CollectionUtils.isEmpty(interfaceInfoList)) {
                return interfaceInfoVOPage;
            }
            interfaceInfoVOPage.setRecords(interfaceInfoList);
            return interfaceInfoVOPage;
        }

        @Override
        public List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit) {
            return userInterfaceInfoMapper.listTopInvokeInterfaceInfo(limit);
        }
    }

