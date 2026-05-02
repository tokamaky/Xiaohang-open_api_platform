package com.xiaohang.project.service;

import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GithubOAuthService {

    String getGithubAuthUrl(HttpServletRequest request, String state);

    LoginUserVO handleGithubCallback(String code, HttpServletRequest request, HttpServletResponse response);

    boolean bindGithubAccount(HttpServletRequest request, String githubId);

    LoginUserVO unbindGithubAccount(HttpServletRequest request);

    /**
     * Validates the CSRF token embedded in the GitHub callback state parameter.
     * Returns the original redirect URL if valid.
     * @throws BusinessException if state is missing, tampered, or expired.
     */
    String validateAndExtractRedirectUrl(String state);

    /**
     * Extracts just the redirect URL portion from the state parameter — used in error handling
     * when CSRF validation fails and we still need to redirect somewhere.
     */
    String extractRedirectUrlFromState(String state);
}
