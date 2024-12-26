package com.xiaohang.xiaohangapicommon.service;

/**
 * 内部用户接口信息服务
 *
 * @author Xiaohang
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
