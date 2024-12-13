package com.xiaohang.project.model.dto.interfaceinfo;

import com.xiaohang.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * request for query
 *
 * @author xiaohang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

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
     * Interface Status (0 - Disabled, 1 - Enabled)
     */
    private Integer status;

    /**
     * Request Method
     */
    private String method;

    /**
     * Creator User ID
     */
    private Long userId;

}