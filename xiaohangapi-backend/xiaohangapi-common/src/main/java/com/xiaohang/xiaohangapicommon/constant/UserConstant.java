package com.xiaohang.xiaohangapicommon.constant;

/**
 * 用户常量
 *
 * @author xiaohang
 */
public interface UserConstant {

    /**
     * Key used for storing the user login state in session.
     */
    String USER_LOGIN_STATE = "userLoginState";

    /**
     * System user ID for a virtual/system-level user.
     */
    long SYSTEM_USER_ID = 0;

    //  region Permissions

    /**
     * Default role assigned to a regular user.
     */
    String DEFAULT_ROLE = "user";

    /**
     * Role assigned to administrators with higher privileges.
     */
    String ADMIN_ROLE = "admin";

    // endregion
}

