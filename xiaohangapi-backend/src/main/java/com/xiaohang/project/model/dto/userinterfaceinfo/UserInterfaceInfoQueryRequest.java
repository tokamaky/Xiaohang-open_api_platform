package com.xiaohang.project.model.dto.userinterfaceinfo;

import com.xiaohang.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Query Request
 *
 * @author Xiaohang
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * Primary key
     */
    private Long id;

    /**
     * User ID making the API call
     */
    private Long userId;

    /**
     * API ID
     */
    private Long interfaceInfoId;

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

}
