package com.xiaohang.xiaohangapicommon.model.vo;


import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaohang
 */
@Data
public class RequestParamsRemarkVO implements Serializable {

    private static final long serialVersionUID = -6549477882078242340L;

    /**
     * ID
     */
    private Long id;

    /**
     * Name
     */
    private String name;

    /**
     * Is Required
     */
    private String isRequired;

    /**
     * Type
     */
    private String type;

    /**
     * Remark
     */
    private String remark;
}
