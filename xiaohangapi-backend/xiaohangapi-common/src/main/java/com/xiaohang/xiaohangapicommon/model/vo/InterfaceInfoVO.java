package com.xiaohang.xiaohangapicommon.model.vo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.google.gson.Gson;
import com.xiaohang.xiaohangapicommon.model.entity.InterfaceInfo;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Interface Information Encapsulation View
 *
 * @author xiaohang
 *
 */
@Data
public class InterfaceInfoVO implements Serializable {

    private final static Gson GSON = new Gson();
    /**
     * Primary key
     */
    @TableId(type = IdType.AUTO)
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
     * Request parameters
     */
    private String requestParams;

    /**
     * Request parameter remarks
     */
    private List<RequestParamsRemarkVO> requestParamsRemark;

    /**
     * Response parameter remarks
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
     * Interface status (0-closed, 1-open)
     */
    private Integer status;

    /**
     * Request method type
     */
    private String method;

    /**
     * Creator ID
     */
    private Long userId;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Update time
     */
    private Date updateTime;

    /**
     * Creator information
     */
    private UserVO user;

    /**
     * Total number of invocations
     */
    private Integer totalNum;

    /**
     * Remaining number of invocations
     */
    private Integer leftNum;

    /**
     * Whether this interface is owned by the current user
     */
    private Boolean isOwnerByCurrentUser;

    /**
     * Convert wrapper class to object
     *
     * @param interfaceInfoVO
     * @return
     */
    public static InterfaceInfo voToObj(InterfaceInfoVO interfaceInfoVO) {
        if (interfaceInfoVO == null) {
            return null;
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoVO, interfaceInfo);
        return interfaceInfo;
    }

    /**
     * Convert object to wrapper class
     *
     * @param interfaceInfo
     * @return
     */
    public static InterfaceInfoVO objToVo(InterfaceInfo interfaceInfo) {
        if (interfaceInfo == null) {
            return null;
        }
        InterfaceInfoVO interfaceInfoVO = new InterfaceInfoVO();
        BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
        return interfaceInfoVO;
    }
}
