package com.xiaohang.project.service.impl;

import static com.xiaohang.project.constant.UserConstant.USER_LOGIN_STATE;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaohang.project.common.ErrorCode;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.mapper.UserMapper;
import com.xiaohang.project.service.UserService;
import com.xiaohang.project.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.xiaohang.project.constant.UserConstant.ADMIN_ROLE;
import static com.xiaohang.project.constant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户服务实现类
 *
 * @author xiaohang
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * Salt value for password encryption
     */
    private static final String SALT = "xiaohang";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. Validation
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameters cannot be empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account is too short");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Password is too short");
        }
        // Ensure passwords match
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Passwords do not match");
        }

        synchronized (userAccount.intern()) {
            // Ensure account is unique
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "Account already exists");
            }

            // 2. Encrypt the password
            String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

            // 3. Generate accessKey and secretKey
            String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));
            // 4. Save user information to the database
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptedPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Registration failed due to database error");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. Validate inputs
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Parameters cannot be empty");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid account");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid password");
        }

        // 2. Encrypt the password
        String encryptedPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // Check if the user exists
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptedPassword);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            log.info("User login failed: account and password do not match");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "User does not exist or password is incorrect");
        }

        // 3. Store login state in session
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return user;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // Check if the user is logged in
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // Retrieve user from the database for accuracy
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // Check if the user has admin privileges
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && ADMIN_ROLE.equals(user.getUserRole());
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // Ensure the user is logged in
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Not logged in");
        }

        // Remove login state
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }
}
