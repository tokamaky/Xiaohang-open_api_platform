package com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * Test Invocation Request Class
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    private Long id;

    /**
     * Request method
     */
    private String method;

    /**
     * Request parameters
     */
    private String requestParams;

    /**
     * Host name
     */
    private String host;
}

