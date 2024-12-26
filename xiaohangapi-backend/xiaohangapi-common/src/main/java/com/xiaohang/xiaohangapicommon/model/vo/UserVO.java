package com.xiaohang.project.model.vo;

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
     * User ID
     */
    private Long id;

    /**
     * User nickname
     */
    private String userName;

    /**
     * Account
     */
    private String userAccount;

    /**
     * User avatar
     */
    private String userAvatar;

    /**
     * Gender
     */
    private Integer gender;

    /**
     * User role: user, admin
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