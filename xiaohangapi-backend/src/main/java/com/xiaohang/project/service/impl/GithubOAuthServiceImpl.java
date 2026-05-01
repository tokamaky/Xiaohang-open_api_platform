package com.xiaohang.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaohang.project.exception.BusinessException;
import com.xiaohang.project.service.GithubOAuthService;
import com.xiaohang.project.service.UserService;
import com.xiaohang.project.utils.JwtUtils;
import com.xiaohang.xiaohangapicommon.common.ErrorCode;
import com.xiaohang.xiaohangapicommon.constant.UserConstant;
import com.xiaohang.xiaohangapicommon.model.entity.User;
import com.xiaohang.xiaohangapicommon.model.vo.LoginUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class GithubOAuthServiceImpl implements GithubOAuthService {

    private static final String GITHUB_CLIENT_ID;
    private static final String GITHUB_CLIENT_SECRET;

    static {
        GITHUB_CLIENT_ID = System.getenv("GITHUB_CLIENT_ID") != null
                ? System.getenv("GITHUB_CLIENT_ID") : "your-github-client-id";
        GITHUB_CLIENT_SECRET = System.getenv("GITHUB_CLIENT_SECRET") != null
                ? System.getenv("GITHUB_CLIENT_SECRET") : "your-github-client-secret";
    }

    private static final String GITHUB_AUTHORIZE_URL = "https://github.com/login/oauth/authorize";
    private static final String GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String GITHUB_USER_INFO_URL = "https://api.github.com/user";

    private static final String SALT = "xiaohang";
    private static final String OAUTH_STATE_SESSION_KEY = "github_oauth_state";
    private static final String OAUTH_RESULT_SESSION_KEY = "github_oauth_result";

    @Resource
    private UserService userService;

    @Resource
    private JwtUtils jwtUtils;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String clientSecret;

    @Override
    public String getGithubAuthUrl(String state) {
        String csrfToken = RandomUtil.randomNumbers(32);
        String redirectUri = buildCallbackUrl();

        StringBuilder url = new StringBuilder(GITHUB_AUTHORIZE_URL);
        url.append("?client_id=").append(clientId);
        url.append("&redirect_uri=").append(java.net.URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        url.append("&scope=read:user,user:email");
        url.append("&state=").append(state).append("_").append(csrfToken);

        return url.toString();
    }

    @Override
    public LoginUserVO handleGithubCallback(String code, HttpServletRequest request, HttpServletResponse response) {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Missing authorization code");
        }

        // Exchange code for access token
        String accessToken = exchangeCodeForToken(code);

        // Get GitHub user info
        Map<String, Object> githubUser = getGithubUserInfo(accessToken);
        String githubId = String.valueOf(githubUser.get("id"));
        String githubLogin = (String) githubUser.get("login");
        String githubAvatar = (String) githubUser.get("avatar_url");

        // Query existing user by githubId
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("githubId", githubId);
        User existingUser = userService.getOne(queryWrapper);

        // Check if user is already logged in via session
        User loginUser = null;
        try {
            loginUser = userService.getLoginUserPermitNull(request);
        } catch (Exception ignored) {
        }

        LoginUserVO resultVO;

        if (existingUser != null && loginUser != null && existingUser.getId().equals(loginUser.getId())) {
            // Already linked and logged in — just return
            resultVO = userService.getLoginUserVO(existingUser);
            storeOAuthResult(request, resultVO);
            return resultVO;
        }

        if (existingUser != null && loginUser != null && !existingUser.getId().equals(loginUser.getId())) {
            // GitHub account already bound to a different user
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "This GitHub account is already linked to another user");
        }

        if (existingUser != null) {
            // Known user, just log them in via JWT
            String token = jwtUtils.generateToken(existingUser.getId(), existingUser.getUserAccount());
            resultVO = userService.getLoginUserVO(existingUser);
            resultVO.setToken(token);
        } else if (loginUser != null) {
            // Logged in user — bind GitHub account
            loginUser.setGithubId(githubId);
            if (StringUtils.isBlank(loginUser.getUserAvatar())) {
                loginUser.setUserAvatar(githubAvatar);
            }
            userService.updateById(loginUser);
            resultVO = userService.getLoginUserVO(loginUser);
        } else {
            // New user — auto-register
            User newUser = createGithubUser(githubId, githubLogin, githubAvatar);
            String token = jwtUtils.generateToken(newUser.getId(), newUser.getUserAccount());
            resultVO = userService.getLoginUserVO(newUser);
            resultVO.setToken(token);
        }

        storeOAuthResult(request, resultVO);
        return resultVO;
    }

    @Override
    public boolean bindGithubAccount(HttpServletRequest request, String githubId) {
        User loginUser = userService.getLoginUser(request);

        // Check if already bound to another account
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("githubId", githubId);
        User existing = userService.getOne(queryWrapper);
        if (existing != null && !existing.getId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "This GitHub account is already linked to another user");
        }

        loginUser.setGithubId(githubId);
        return userService.updateById(loginUser);
    }

    @Override
    public boolean unbindGithubAccount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        loginUser.setGithubId(null);
        return userService.updateById(loginUser);
    }

    public LoginUserVO getOAuthResult(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return null;
        Object result = session.getAttribute(OAUTH_RESULT_SESSION_KEY);
        if (result instanceof LoginUserVO) {
            session.removeAttribute(OAUTH_RESULT_SESSION_KEY);
            return (LoginUserVO) result;
        }
        return null;
    }

    private String buildCallbackUrl() {
        return "https://" + getBackendHost() + "/api/oauth/github/callback";
    }

    private String getBackendHost() {
        String host = System.getenv("BACKEND_HOST");
        if (host != null) return host;
        return "backend-production-796b.up.railway.app";
    }

    private String exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(java.util.Collections.singletonList(
                new org.springframework.http.MediaType("application", "json")));

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
        String response = restTemplate.postForObject(GITHUB_TOKEN_URL, entity, String.class);

        // Parse access_token from response (GitHub returns: access_token=xxx&...)
        if (response == null || !response.contains("access_token=")) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to exchange GitHub code for token");
        }

        String token = response.substring(response.indexOf("access_token=") + 13);
        if (token.contains("&")) {
            token = token.substring(0, token.indexOf("&"));
        }
        return token.trim();
    }

    private Map<String, Object> getGithubUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + accessToken);
        headers.set("Accept", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);
        org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                GITHUB_USER_INFO_URL,
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getBody() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to get GitHub user info");
        }
        return response.getBody();
    }

    private User createGithubUser(String githubId, String githubLogin, String githubAvatar) {
        String userAccount = "github_" + githubLogin;
        String accessKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT + userAccount + RandomUtil.randomNumbers(8));

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserName(githubLogin);
        user.setUserAvatar(githubAvatar);
        user.setGithubId(githubId);
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        user.setUserRole("user");

        boolean saved = userService.save(user);
        if (!saved) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "GitHub account registration failed");
        }
        return user;
    }

    private void storeOAuthResult(HttpServletRequest request, LoginUserVO vo) {
        HttpSession session = request.getSession(true);
        session.setAttribute(OAUTH_RESULT_SESSION_KEY, vo);
    }
}
