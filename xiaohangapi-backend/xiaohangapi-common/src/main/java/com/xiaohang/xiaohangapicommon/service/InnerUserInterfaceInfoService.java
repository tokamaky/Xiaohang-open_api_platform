package com.xiaohang.xiaohangapicommon.service;

import com.xiaohang.xiaohangapicommon.model.entity.UserInterfaceInfo;

/**
 * Internal User Interface Information Service
 *
 * @author Xiaohang
 */
public interface InnerUserInterfaceInfoService {

    /**
     * Count the invocation of an interface.
     *
     * @param interfaceInfoId The ID of the interface.
     * @param userId          The ID of the user.
     * @return boolean Whether the operation was successful.
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * Check if there are remaining invocation counts.
     *
     * @param interfaceId The ID of the interface.
     * @param userId      The ID of the user.
     * @return UserInterfaceInfo The user interface information.
     */
    UserInterfaceInfo hasLeftNum(Long interfaceId, Long userId);

    /**
     * Add default user interface information.
     *
     * @param interfaceId The ID of the interface.
     * @param userId      The ID of the user.
     * @return Boolean Whether the addition was successful.
     */
    Boolean addDefaultUserInterfaceInfo(Long interfaceId, Long userId);

    /**
     * Check if a user has access to an interface.
     *
     * @param interfaceId The ID of the interface.
     * @param userId      The ID of the user.
     * @return UserInterfaceInfo The user interface information.
     */
    UserInterfaceInfo checkUserHasInterface(long interfaceId, long userId);
}
