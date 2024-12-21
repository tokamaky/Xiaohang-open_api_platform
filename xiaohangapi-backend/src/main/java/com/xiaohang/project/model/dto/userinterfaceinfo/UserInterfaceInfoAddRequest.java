package com.xiaohang.project.model.dto.userinterfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * Create Request
 *
 * @author xiaohang
 */
@Data
public class UserInterfaceInfoAddRequest implements Serializable {

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

}
