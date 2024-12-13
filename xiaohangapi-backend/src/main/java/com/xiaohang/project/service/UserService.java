package com.xiaohang.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.model.entity.User;
import javax.servlet.http.HttpServletRequest;

/**
 * user service
 *
 * @author xiaohang
 */
public interface UserService extends IService<User> {

    /**
     * User registration
     *
     * @param userAccount   User account
     * @param userPassword  User password
     * @param checkPassword Password confirmation
     * @return ID of the newly created user
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * User login
     *
     * @param userAccount  User account
     * @param userPassword User password
     * @param request      HTTP request object
     * @return User information with sensitive data masked
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * Get the currently logged-in user
     *
     * @param request HTTP request object
     * @return The current logged-in user's information
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * Check if the current user is an admin
     *
     * @param request HTTP request object
     * @return true if the user is an admin, false otherwise
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * User logout
     *
     * @param request HTTP request object
     * @return true if logout is successful, false otherwise
     */
    boolean userLogout(HttpServletRequest request);
}
