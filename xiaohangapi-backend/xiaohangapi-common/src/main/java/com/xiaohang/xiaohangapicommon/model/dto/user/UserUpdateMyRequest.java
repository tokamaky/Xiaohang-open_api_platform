package com.xiaohang.xiaohangapicommon.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User Update Personal Information Request
 */
@Data
public class UserUpdateMyRequest implements Serializable {

    /**
     * User nickname
     */
    private String userName;

    /**
     * User avatar
     */
    private String userAvatar;

    /**
     * Profile description
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}

