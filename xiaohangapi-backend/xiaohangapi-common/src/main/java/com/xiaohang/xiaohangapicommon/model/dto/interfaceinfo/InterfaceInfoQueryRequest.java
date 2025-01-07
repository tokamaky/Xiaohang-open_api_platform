package com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaohang.xiaohangapicommon.common.PageRequest;
import com.xiaohang.xiaohangapicommon.model.vo.RequestParamsRemarkVO;
import com.xiaohang.xiaohangapicommon.model.vo.ResponseParamsRemarkVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Query Request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Search term
     */
    private String searchText;

    /**
     * Primary Key
     */
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
     * Host name
     */
    private String host;

    /**
     * Interface URL
     */
    private String url;

    /**
     * Request parameters description
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * Response parameters description
     */
    private List<ResponseParamsRemarkVO> responseParamsRemark;

    /**
     * Request headers
     */
    private String requestHeader;

    /**
     * Response headers
     */
    private String responseHeader;

    /**
     * Interface status (0 - closed, 1 - open)
     */
    private Integer status;

    /**
     * Request method
     */
    private String method;

    /**
     * Creator ID
     */
    private Long userId;

    /**
     * Creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * Is deleted (0 - not deleted, 1 - deleted)
     */
    private Integer isDelete;
}
