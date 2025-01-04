package com.xiaohang.xiaohangapicommon.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User Update Request
 */
@Data
public class UserUpdateRequest implements Serializable {
    /**
     * ID
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
     * Profile description
     */
    private String userProfile;

    /**
     * User role: user/admin/ban
     */
    private String userRole;

    private static final long serialVersionUID = 1L;
}
