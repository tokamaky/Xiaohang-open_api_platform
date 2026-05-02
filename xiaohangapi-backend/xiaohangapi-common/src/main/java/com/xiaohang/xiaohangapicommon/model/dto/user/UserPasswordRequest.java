package com.xiaohang.xiaohangapicommon.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * User Password Request
 * Used for password verification (delete account) and password change
 */
@Data
public class UserPasswordRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120794L;

    /**
     * Current password (for verification or old password for change)
     */
    private String userPassword;

    /**
     * New password (only for password change)
     */
    private String newPassword;
}
