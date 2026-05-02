package com.xiaohang.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaohang.project.service.GithubOAuthService;
import com.xiaohang.project.service.impl.GithubOAuthServiceImpl;
import com.xiaohang.project.utils.JwtUtils;
import com.xiaohang.xiaohangapicommon.common.BaseResponse;
import com.xiaohang.xiaohangapicommon.common.ResultUtils;
import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    @Resource
    private JwtUtils jwtUtils;

    /**
     * Get GitHub OAuth authorization URL.
     * The 'redirectUrl' param tells backend where to redirect after OAuth completes.
     * We pass HttpServletRequest so the service can associate the CSRF token with the session.
     */
    @GetMapping("/github/url")
    public BaseResponse<String> getGithubAuthUrl(
            @RequestParam String redirectUrl,
            HttpServletRequest request) {
        String url = githubOAuthService.getGithubAuthUrl(request, redirectUrl);
        return ResultUtils.success(url);
    }

    /**
     * GitHub OAuth callback — GitHub redirects here after user authorizes.
     * Validates the CSRF state, then encodes the login result (token + user info) into the
     * redirect URL fragment so it survives Railway serverless container switches.
     */
    @GetMapping("/github/callback")
    public void githubCallback(
            @RequestParam String code,
            @RequestParam String state,
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            // Validate CSRF state before processing — extracts and verifies the redirect URL
            String redirectUrl = githubOAuthServiceImpl.validateAndExtractRedirectUrl(state);
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "/";
            }

            LoginUserVO vo = githubOAuthService.handleGithubCallback(code, request, response);
            log.info("[OAuth] Building callback payload — userAccount: {}, userName: {}, githubId: {}, token generated: true",
                    vo.getUserAccount(), vo.getUserName(), vo.getGithubId());

            // Always regenerate token here so it works regardless of which serverless
            // container handled the callback (containers share no state).
            String token = jwtUtils.generateToken(vo.getId(), vo.getUserAccount());
            Map<String, Object> payload = new HashMap<>();
            payload.put("token", token);
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
            log.error("GitHub OAuth callback error: {}", e.getMessage(), e);
            String redirectUrl;
            try {
                redirectUrl = githubOAuthServiceImpl.extractRedirectUrlFromState(state);
            } catch (Exception ex) {
                redirectUrl = "/";
            }
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            String finalRedirect = redirectUrl + "?__oauth_error=1&error=" + errorMsg;
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
