package com.xiaohang.xiaohangapicommon.model.dto.userinterfaceinfo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xiaohang.xiaohangapicommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


/**
 * Query Request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserInterfaceInfoQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Primary Key
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Search term
     */
    private String searchText;

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

    /**
     * 0 - active, 1 - disabled
     */
    private Integer status;
}
