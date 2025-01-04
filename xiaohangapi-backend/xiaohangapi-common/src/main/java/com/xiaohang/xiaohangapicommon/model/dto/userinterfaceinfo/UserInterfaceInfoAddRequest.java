package com.xiaohang.xiaohangapicommon.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * Create Request
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Calling user ID
     */
    private Long userId;

    /**
     * Interface ID
     */
    private Long interfaceInfoId;

    /**
     * Total number of invocations
     */
    private Integer totalNum;

    /**
     * Remaining number of invocations
     */
    private Integer leftNum;
}
