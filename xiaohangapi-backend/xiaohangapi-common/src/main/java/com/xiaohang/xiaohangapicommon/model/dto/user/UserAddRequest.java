package com.xiaohang.xiaohangapicommon.model.dto.user;

import lombok.Data;

import java.io.Serializable;


/**
 * User Creation Request
 */
@Data
public class UserAddRequest implements Serializable {

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
     * User role: user, admin
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
