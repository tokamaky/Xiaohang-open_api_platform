package com.xiaohang.xiaohangapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Interface Information
 *
 * @author xiaohang
 */
@TableName(value ="interface_info")
@Data
public class InterfaceInfo implements Serializable {
    /**
     * Primary key
     */
    @TableId(type = IdType.AUTO)
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
     * Host name
     */
    private String host;

    /**
     * Request parameters
     */
    private String requestParams;

    /**
     * Request parameter remarks
     */
    private String requestParamsRemark;

    /**
     * Response parameter remarks
     */
    private String responseParamsRemark;

    /**
     * Request headers
     */
    private String requestHeader;

    /**
     * Response headers
     */
    private String responseHeader;

    /**
     * Interface status (0-closed, 1-open)
     */
    private Integer status;

    /**
     * Request method type
     */
    private String method;

    /**
     * Creator ID
     */
    private Long userId;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Update time
     */
    private Date updateTime;

    /**
     * Is deleted (0-not deleted, 1-deleted)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
