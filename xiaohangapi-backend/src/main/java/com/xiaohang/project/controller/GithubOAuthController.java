package com.xiaohang.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaohang.project.common.UserConstant;
import com.xiaohang.project.model.User;
import com.xiaohang.project.service.GithubOAuthService;
import com.xiaohang.project.service.UserService;
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

    @Resource
    private UserService userService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * Get GitHub OAuth authorization URL.
     * The 'redirectUrl' param tells backend where to redirect after OAuth completes.
     * The 'action' param can be "link" to indicate this is a link operation (not login).
     * For "link" action, we store the current user's ID in session for verification during callback.
     */
    @GetMapping("/github/url")
    public BaseResponse<String> getGithubAuthUrl(
            @RequestParam String redirectUrl,
            @RequestParam(required = false, defaultValue = "login") String action,
            HttpServletRequest request) {
        // If this is a link action, store the current user ID in session for verification during callback
        if ("link".equals(action)) {
            try {
                User loginUser = userService.getLoginUser(request);
                request.getSession().setAttribute("oauth_link_user_id", loginUser.getId());
                log.info("[OAuth] Storing link user ID in session: {}", loginUser.getId());
            } catch (Exception e) {
                log.warn("[OAuth] Failed to get current user for link action: {}", e.getMessage());
                // Continue anyway - the user might not be logged in and we'll catch that in callback
            }
        }

        String state = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8) + "_" + action;
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
            // Extract action from state (format: "encodedRedirectUrl_action")
            String action = "login";
            int lastUnderscore = state.lastIndexOf('_');
            if (lastUnderscore > 0) {
                String possibleAction = state.substring(lastUnderscore + 1);
                // Only use it if it's a known action
                if ("link".equals(possibleAction)) {
                    action = possibleAction;
                    state = state.substring(0, lastUnderscore);
                }
            }

            LoginUserVO vo = githubOAuthService.handleGithubCallback(code, request, response, action);
            String redirectUrl = URLDecoder.decode(state, StandardCharsets.UTF_8);
            if (redirectUrl == null || redirectUrl.isEmpty()) {
                redirectUrl = "/";
            }
            // Always regenerate token here in the controller so it works regardless of
            // which serverless container handled the callback (containers share no state).
            String token = jwtUtils.generateToken(vo.getId(), vo.getUserAccount());
            log.info("[OAuth] Building callback payload — action: {}, userAccount: {}, userName: {}, githubId: {}, token generated: {}",
                    action, vo.getUserAccount(), vo.getUserName(), vo.getGithubId(), token != null);
            Map<String, Object> payload = new HashMap<>();
            payload.put("token", token);
            payload.put("userAccount", vo.getUserAccount());
            payload.put("userName", vo.getUserName());
            payload.put("userAvatar", vo.getUserAvatar());
            payload.put("userRole", vo.getUserRole());
            payload.put("githubId", vo.getGithubId());
            payload.put("id", vo.getId());
            payload.put("isNew", vo.getUserAccount() != null && vo.getUserAccount().startsWith("github_"));
            payload.put("oauthAction", action);

            String json = new ObjectMapper().writeValueAsString(payload);
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            // Use hash fragment (#) instead of query params (?).
            // Query params are sent to the server — Railway returns 404 for /user/login_xxx
            // and the 404.html JS may not run if the response is cached or cold-started.
            // Hash fragments are never sent to the server; the browser always loads index.html
            // and the frontend reads the OAuth data from window.location.hash.
            response.sendRedirect(redirectUrl + "#__oauth_done=1&__oauth_data=" + encoded);
        } catch (Exception e) {
            log.error("GitHub OAuth callback error: {}", e.getMessage());
            String redirectUrl = URLDecoder.decode(state, StandardCharsets.UTF_8);
            String errorMsg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            String finalRedirect = (redirectUrl != null ? redirectUrl : "/") + "#__oauth_error=1&error=" + errorMsg;
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
