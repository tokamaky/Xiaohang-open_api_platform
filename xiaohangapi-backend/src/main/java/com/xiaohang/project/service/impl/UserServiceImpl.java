package com.xiaohang.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.mapper.UserMapper;
import com.xiaohang.project.service.UserService;
import com.xiaohang.project.utils.SqlUtils;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.constant.CommonConstant;
import com.xiaohang.xiaohangapicommon.model.dto.user.UserQueryRequest;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.enums.UserRoleEnum;
import com.xiaohang.xiaohangapicommon.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.xiaohang.xiaohangapicommon.constant.UserConstant.USER_LOGIN_STATE;


/**
 * User service implementation
 *
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * Salt value, obfuscates password
     */
    private static final String SALT = "xiaohang";

    @Resource
    private com.xiaohang.project.utils.JwtUtils jwtUtils;

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. Validation
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameters are empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User account is too short");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User password is too short");
        }
        // Password and check password must be the same
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "The two passwords do not match");
        }
        synchronized (userAccount.intern()) {
            // Account cannot be duplicated
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account is duplicated");
            }
            // 2. Encrypt password
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. Assign accessKey, secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. Insert data
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(StringUtils.upperCase(userAccount));
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            user.setUserAvatar("https://image-bed-ichensw.oss-cn-hangzhou.aliyuncs.com/Multiavatar-f5871c303317a4dafbf6.png");
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Registration failed, database error");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. Validation
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameters are empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Incorrect account");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Incorrect password");
        }
        // 2. Encrypt password
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // Check if user exists
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // User does not exist
        if (user == null) {
            log.info("User login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User does not exist or password is incorrect");
        }
        // 3. Record the user's login status
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }



    /**
     * Get the current logged-in user
     * Supports both Session-based and JWT token-based authentication
     *
     * @param request HTTP request
     * @return Current logged-in user
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // First, try to get user from Session (for regular login)
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;

        // If not in session, try JWT token (for GitHub OAuth login)
        if (currentUser == null || currentUser.getId() == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtils.validateToken(token)) {
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        if (userId != null) {
                            currentUser = this.getById(userId);
                            if (currentUser != null) {
                                return currentUser;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to validate JWT token: {}", e.getMessage());
                }
            }
            throw new BusinessException(ErrorCode.FIRST_TIME_LOGIN);
        }

        // Query from the database to get fresh data
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * Get the current logged-in user (allow null if not logged in)
     * Supports both Session-based and JWT token-based authentication
     *
     * @param request HTTP request
     * @return Current logged-in user or null if not logged in
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // First, try to get user from Session (for regular login)
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;

        // If not in session, try JWT token (for GitHub OAuth login)
        if (currentUser == null || currentUser.getId() == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtils.validateToken(token)) {
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        if (userId != null) {
                            currentUser = this.getById(userId);
                            if (currentUser != null) {
                                return currentUser;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to validate JWT token: {}", e.getMessage());
                }
            }
            return null;
        }

        // Query from the database to get fresh data
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * Check if the user is an admin
     * Supports both Session-based and JWT token-based authentication
     *
     * @param request HTTP request
     * @return boolean
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // First, try to get user from Session
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;

        // If not in session, try JWT token (for GitHub OAuth login)
        if (user == null || user.getId() == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtils.validateToken(token)) {
                        Long userId = jwtUtils.getUserIdFromToken(token);
                        if (userId != null) {
                            user = this.getById(userId);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to validate JWT token in isAdmin: {}", e.getMessage());
                }
            }
        }
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * User logout
     * Supports both Session-based and JWT token-based authentication
     *
     * @param request HTTP request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // Check session first
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            // If not in session, check JWT token (for GitHub OAuth users)
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    if (jwtUtils.validateToken(token)) {
                        // Valid JWT token, consider as logged in
                        // For JWT-based auth, we don't have session to clear
                        return true;
                    }
                } catch (Exception e) {
                    log.warn("Failed to validate JWT token on logout: {}", e.getMessage());
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Not logged in");
        }
        // Remove login status
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Request parameters are empty");
        }
        Long id = userQueryRequest.getId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);

        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean updateSecretKey(Long id) {
        User user = this.getById(id);
        String accessKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + user.getUserAccount() + RandomUtil.randomNumbers(8));
        user.setSecretKey(secretKey);
        user.setAccessKey(accessKey);
        return this.updateById(user);
    }

    @Override
    public boolean deleteMyAccount(String userPassword, HttpServletRequest request) {
        User loginUser = getLoginUser(request);

        // Check if user is a GitHub OAuth user (has password placeholder)
        // GitHub OAuth users have password = md5(salt + githubId + "github_oauth")
        boolean isGithubUser = loginUser.getUserPassword() != null &&
                loginUser.getUserPassword().endsWith("github_oauth") &&
                loginUser.getGithubId() != null;

        if (isGithubUser) {
            // For GitHub OAuth users, verify using their GitHub account via JWT token
            // They must be authenticated with a valid JWT token to delete
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtUtils.validateToken(token)) {
                    // Valid JWT token from GitHub OAuth, allow deletion without password
                    // Clear session
                    request.getSession().removeAttribute(USER_LOGIN_STATE);
                    return this.removeById(loginUser.getId());
                }
            }
            // If no valid JWT token, reject
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "Cannot delete GitHub OAuth account without valid authentication. Please log in via GitHub first.");
        }

        // For regular users, verify password
        if (StringUtils.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password is required");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", loginUser.getId());
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Incorrect password");
        }
        // Delete the user
        boolean result = this.removeById(loginUser.getId());
        if (result) {
            // Clear session
            request.getSession().removeAttribute(USER_LOGIN_STATE);
        }
        return result;
    }

    @Override
    public boolean changePassword(String oldPassword, String newPassword, HttpServletRequest request) {
        if (StringUtils.isAnyBlank(oldPassword, newPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Old password and new password are required");
        }
        if (newPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "New password must be at least 8 characters");
        }
        User loginUser = getLoginUser(request);

        // Check if user is a GitHub OAuth user (has password placeholder)
        boolean isGithubUser = loginUser.getUserPassword() != null &&
                loginUser.getUserPassword().endsWith("github_oauth") &&
                loginUser.getGithubId() != null;

        if (isGithubUser) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "Cannot change password for GitHub OAuth account. Your account was created using GitHub OAuth.");
        }

        // Verify old password
        String encryptOldPassword = DigestUtils.md5DigestAsHex((SALT + oldPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", loginUser.getId());
        queryWrapper.eq("userPassword", encryptOldPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Incorrect old password");
        }
        // Update password
        String encryptNewPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        user.setUserPassword(encryptNewPassword);
        return this.updateById(user);
    }
}