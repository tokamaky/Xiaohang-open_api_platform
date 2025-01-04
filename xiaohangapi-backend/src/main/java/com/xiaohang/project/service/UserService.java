package com.xiaohang.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import com.xiaohang.xiaohangapicommon.model.dto.user.UserQueryRequest;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * @param checkPassword Check password
     * @return New user id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * User login
     *
     * @param userAccount  User account
     * @param userPassword User password
     * @param request
     * @return Masked user information
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * Get current logged-in user
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * Get current logged-in user (allow null)
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * Check if the user is an administrator
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * Check if the user is an administrator
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * User logout
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * Get masked user information for the logged-in user
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * Get masked user information
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * Get masked user information
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * Get query conditions
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * Update secretKey
     *
     * @param id User id
     * @return boolean
     */
    boolean updateSecretKey(Long id);
}