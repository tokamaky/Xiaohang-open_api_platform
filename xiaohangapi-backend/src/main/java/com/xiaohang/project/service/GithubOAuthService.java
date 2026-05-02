package com.xiaohang.project.service;

import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface GithubOAuthService {

    String getGithubAuthUrl(String state);

    LoginUserVO handleGithubCallback(String code, HttpServletRequest request, HttpServletResponse response);

    boolean bindGithubAccount(HttpServletRequest request, String githubId);

    LoginUserVO unbindGithubAccount(HttpServletRequest request);
}
