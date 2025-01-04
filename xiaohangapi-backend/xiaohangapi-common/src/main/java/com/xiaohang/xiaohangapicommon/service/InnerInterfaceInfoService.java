package com.xiaohang.xiaohangapicommon.service;


import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;

/**
 * Internal Interface Information Service
 *
 * @author Xiaohang
 */
public interface InnerInterfaceInfoService {

    /**
     * Retrieve information about a mock interface from the database
     * based on the request path, method, and parameters.
     *
     * @param path   The request path.
     * @param method The request method.
     * @return InterfaceInfo The interface information.
     */
    InterfaceInfo getInterfaceInfo(String path, String method);

}
