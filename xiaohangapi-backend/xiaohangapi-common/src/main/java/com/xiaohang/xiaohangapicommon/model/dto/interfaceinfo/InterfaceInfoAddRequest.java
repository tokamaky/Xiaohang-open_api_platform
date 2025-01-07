package com.xiaohang.xiaohangapicommon.model.dto.interfaceinfo;


import com.xiaohang.xiaohangapicommon.model.vo.RequestParamsRemarkVO;
import com.xiaohang.xiaohangapicommon.model.vo.ResponseParamsRemarkVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
/**
 * Create Request
 */
@Data
public class InterfaceInfoAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private String requestParams;

    /**
     * Request parameters remark
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * Response parameters remark
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
     * Request method (e.g., GET, POST)
     */
    private String method;
}
