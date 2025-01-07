package com.xiaohang.xiaohangapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author xiaohang
 */
@Data
public class ResponseParamsRemarkVO implements Serializable {
    private static final long serialVersionUID = 3673526667928077840L;

    /**
     * ID
     */
    private Long id;

    /**
     * Name
     */
    private String name;

    /**
     * Type
     */
    private String type;

    /**
     * Remark
     */
    private String remark;
}
