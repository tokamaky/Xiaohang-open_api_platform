package com.xiaohang.xiaohangapicommon.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户
 *
 * @TableName user
 */
@TableName(value = "user")
@Data
public class User implements Serializable {

    /**
     * ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * User account
     */
    private String userAccount;

    /**
     * User password
     */
    private String userPassword;


    /**
     * User nickname
     */
    private String userName;

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
     * Access key
     */
    private String accessKey;

    /**
     * Secret key
     */
    private String secretKey;

    /**
     * Creation time
     */
    private Date createTime;

    /**
     * Update time
     */
    private Date updateTime;

    /**
     * Is deleted (0-not deleted, 1-deleted)
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = -7572280859051489820L;
}
