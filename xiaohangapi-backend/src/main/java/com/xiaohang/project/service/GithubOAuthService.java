package com.xiaohang.project.service;

import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GithubOAuthService {

    String getGithubAuthUrl(String state);

    /**
     * Handle GitHub OAuth callback
     * @param code Authorization code from GitHub
     * @param request HTTP request
     * @param response HTTP response
     * @param action The action type: "login" or "link"
     * @return LoginUserVO with user info
     */
    LoginUserVO handleGithubCallback(String code, HttpServletRequest request, HttpServletResponse response, String action);

    boolean bindGithubAccount(HttpServletRequest request, String githubId);

    LoginUserVO unbindGithubAccount(HttpServletRequest request);
}
