package com.xiaohang.xiaohangapicommon.service;

import com.xiaohang.xiaohangapicommon.model.entity.User;

/**
 * Internal User Service
 *
 * @author Xiaohang
 */
public interface InnerUserService {

    /**
     * Check in the database if the access key (accessKey) has already been assigned to a user.
     *
     * @param accessKey The access key to check.
     * @return The user associated with the access key, if found.
     */
    User getInvokeUser(String accessKey);
}