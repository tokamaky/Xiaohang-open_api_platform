package com.xiaohang.xiaohangapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * User View
 *
 * @TableName user
 */
@Data
public class UserVO implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * User nickname
     */
    private String userName;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User avatar
     */
    private String userAvatar;

    /**
     * User profile
     */
    private String userProfile;

    /**
     * User role: user/admin/ban
     */
    private String userRole;

    /**
     * AccessKey
     */
    private String accessKey;

    /**
     * SecretKey
     */
    private String secretKey;

    /**
     * Creation time
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
