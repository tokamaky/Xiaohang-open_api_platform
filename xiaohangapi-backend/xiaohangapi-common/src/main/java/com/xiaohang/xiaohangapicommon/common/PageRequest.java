package com.xiaohang.xiaohangapicommon.common;

import com.xiaohang.xiaohangapicommon.constant.CommonConstant;
import lombok.Data;

/**
 * Pagination Request
 *
 * Represents a request for paginated data.
 *
 * Author: xiaohang
 */
@Data
public class PageRequest {

    /**
     * Current page number (default: 1)
     */
    private long current = 1;

    /**
     * Page size (default: 10)
     */
    private long pageSize = 10;

    /**
     * Sort field
     */
    private String sortField;

    /**
     * Sort order (default: ascending)
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
