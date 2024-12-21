package com.xiaohang.project.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * Update Request
 *
 * @author Xiaohang
 */
@Data
public class UserInterfaceInfoUpdateRequest implements Serializable {

    /**
     * Primary key
     */
    private Long id;

    /**
     * Total number of calls
     */
    private Integer totalNum;

    /**
     * Remaining number of calls
     */
    private Integer leftNum;

    /**
     * Status (0 - Active, 1 - Disabled)
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
