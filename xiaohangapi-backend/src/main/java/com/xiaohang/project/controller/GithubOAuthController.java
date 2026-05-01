package com.xiaohang.project.controller;

import com.xiaohang.project.service.GithubOAuthService;
import com.xiaohang.project.service.impl.GithubOAuthServiceImpl;
import com.xiaohang.xiaohangapicommon.common.BaseResponse;
import com.xiaohang.xiaohangapicommon.common.ResultUtils;
import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/oauth")
@Slf4j
public class GithubOAuthController {

    @Resource
    private GithubOAuthService githubOAuthService;

    @Resource
    private GithubOAuthServiceImpl githubOAuthServiceImpl;

    /**
     * Get GitHub OAuth authorization URL.
     * The 'redirectUrl' param tells backend where to redirect after OAuth completes.
     */
    @GetMapping("/github/url")
    public BaseResponse<String> getGithubAuthUrl(@RequestParam String redirectUrl) {
        String state = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8);
        String url = githubOAuthService.getGithubAuthUrl(state);
        return ResultUtils.success(url);
    }

    /**
     * GitHub OAuth callback — GitHub redirects here after user authorizes.
     * Stores the result in session and redirects back to frontend with a marker.
     * Frontend polls /oauth/github/result to get the actual login data.
     */
    @GetMapping("/github/callback")
    public void githubCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            githubOAuthService.handleGithubCallback(code, request, response);
            String redirectUrl = URLDecoder.decode(state, StandardCharsets.UTF_8);
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "/";
            }
            response.sendRedirect(redirectUrl + "?__oauth_done=1");
        } catch (Exception e) {
            log.error("GitHub OAuth callback error: {}", e.getMessage());
            String redirectUrl = URLDecoder.decode(state, StandardCharsets.UTF_8);
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            String finalRedirect = (redirectUrl != null ? redirectUrl : "/") + "?__oauth_error=1&error=" + errorMsg;
            response.sendRedirect(finalRedirect);
        }
    }

    /**
     * Poll for OAuth result after callback. Frontend calls this after detecting __oauth_done.
     */
    @GetMapping("/github/result")
    public BaseResponse<LoginUserVO> getOAuthResult(HttpServletRequest request) {
        LoginUserVO vo = githubOAuthServiceImpl.getOAuthResult(request);
        return ResultUtils.success(vo);
    }
}
