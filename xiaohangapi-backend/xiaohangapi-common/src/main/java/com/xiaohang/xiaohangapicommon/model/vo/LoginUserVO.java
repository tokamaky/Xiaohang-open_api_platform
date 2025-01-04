package com.xiaohang.xiaohangapicommon.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Logged-in User View (De-identified)
 * @author xiaohang
 **/
@Data
public class LoginUserVO implements Serializable {

    /**
     * User ID
     */
    private Long id;

    /**
     * User nickname
     */
    private String userName;

    /**
     * User avatar
     */
    private String userAvatar;

    /**
     * User profile description
     */
    private String userProfile;

    /**
     * User role: user/admin/ban
     */
    private String userRole;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Update time
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
