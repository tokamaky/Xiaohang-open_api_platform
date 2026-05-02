package com.xiaohang.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

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
     * Encodes the login result (token + user info) into the redirect URL fragment
     * so it survives serverless container switches. The frontend extracts it from
     * window.location.search after the redirect.
     */
    @GetMapping("/github/callback")
    public void githubCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            LoginUserVO vo = githubOAuthService.handleGithubCallback(code, request, response);
            String redirectUrl = URLDecoder.decode(state, StandardCharsets.UTF_8);
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "/";
            }
            // Encode token + user data as base64 JSON so any container can decode it.
            // We include only the essential fields to avoid long URLs.
            Map<String, Object> payload = new HashMap<>();
            payload.put("token", vo.getToken());
            payload.put("userAccount", vo.getUserAccount());
            payload.put("userName", vo.getUserName());
            payload.put("userAvatar", vo.getUserAvatar());
            payload.put("userRole", vo.getUserRole());
            payload.put("githubId", vo.getGithubId());
            payload.put("id", vo.getId());
            payload.put("isNew", vo.getUserAccount() != null && vo.getUserAccount().startsWith("github_"));

            String json = new ObjectMapper().writeValueAsString(payload);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            response.sendRedirect(redirectUrl + "?__oauth_done=1&__oauth_data=" + encoded);
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
