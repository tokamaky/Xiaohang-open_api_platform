package com.xiaohang.xiaohangapicommon.service;


import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;

/**
 * 内部接口信息服务
 *
 * @author Xiaohang
 */
public interface InnerInterfaceInfoService {

    /**
     * 从数据库中查询模拟接口是否存在（请求路径、请求方法、请求参数）
     */
    InterfaceInfo getInterfaceInfo(String path, String method);
    /**
     * 根据path、method查询接口信息
     *
     * @param path   请求路径
     * @param method 请求方法
     * @return InterfaceInfo
     */
    InterfaceInfo getInvokeInterfaceInfo(String path, String method);
}
