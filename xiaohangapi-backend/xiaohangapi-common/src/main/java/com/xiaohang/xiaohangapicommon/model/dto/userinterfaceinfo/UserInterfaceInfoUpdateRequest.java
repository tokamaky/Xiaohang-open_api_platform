package com.xiaohang.xiaohangapicommon.model.dto.userinterfaceinfo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Update Request
 */
@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    private Long id;

    /**
     * Total number of invocations
     */
    private Integer totalNum;

    /**
     * Remaining number of invocations
     */
    private Integer leftNum;

    /**
     * 0 - active, 1 - disabled
     */
    private Integer status;
}
