package com.xiaohang.project.model.dto.interfaceinfo;


import lombok.Data;

import java.io.Serializable;

/**
 * Update Request
 *
 * @author xiaohang
 *  
 */
@Data
public class InterfaceInfoUpdateRequest implements Serializable {

    /**
     * Primary Key
     */
    private Long id;

    /**
     * Name
     */
    private String name;

    /**
     * Description
     */
    private String description;

    /**
     * Interface URL
     */
    private String url;

    /**
     * Request Parameters
     * [
     *   {"name": "username", "type": "string"}
     * ]
     */
    private String requestParams;

    /**
     * Request Header
     */
    private String requestHeader;

    /**
     * Response Header
     */
    private String responseHeader;

    /**
     * Interface Status (0 - Disabled, 1 - Enabled)
     */
    private Integer status;

    /**
     * Request Method
     */
    private String method;

    private static final long serialVersionUID = 1L;
}