package com.xiaohang.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * Request for add post
 *
 * @author xiaohang
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {
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
     * {"name": "username", "type": "string"}
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
     * Request Method
     */
    private String method;


}