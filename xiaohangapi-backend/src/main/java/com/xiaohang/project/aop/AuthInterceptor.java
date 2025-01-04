package com.xiaohang.project.aop;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.xiaohang.project.annotation.AuthCheck;

import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.service.UserService;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.enums.UserRoleEnum;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Permission Check AOP (Aspect-Oriented Programming)
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * Execute interception
     *
     * @param joinPoint The join point representing the method being intercepted
     * @param authCheck The annotation that triggered the interception
     * @return The result of the intercepted method execution
     * @throws Throwable If an error occurs during the method execution
     */
    @Around("@annotation(authCheck)")  // Intercept methods annotated with @AuthCheck
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();  // Get the required role from the annotation
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // Get the currently logged-in user
        User loginUser = userService.getLoginUser(request);

        // If a specific role is required for access
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);  // Throw error if the role is invalid
            }
            String userRole = loginUser.getUserRole();

            // If the user is banned, reject the request
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }

            // If the required role is admin, check if the user has admin role
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);  // Reject if the user doesn't have the required role
                }
            }
        }

        // If the user passes the permission check, allow the method to proceed
        return joinPoint.proceed();
    }
}
