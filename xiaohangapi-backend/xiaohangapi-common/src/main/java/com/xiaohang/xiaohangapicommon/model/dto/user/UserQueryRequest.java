package com.xiaohang.xiaohangapicommon.model.dto.user;


import com.xiaohang.xiaohangapicommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * User Query Request
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * User nickname
     */
    private String userName;

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
