package com.xiaohang.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    // In-memory CSRF state cache: csrfToken -> original redirectUrl
    // Entries expire after 10 minutes. This works across Railway serverless instances
    // because the state is validated purely from the cache, not from session.
    private static final Map<String, String> requestCache = new ConcurrentHashMap<>();

    @Resource
    private UserService userService;

    @Resource
    private JwtUtils jwtUtils;

    @Value("${spring.security.oauth2.client.registration.github.client-id:}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret:}")
    private String clientSecret;

    @Override
    public String getGithubAuthUrl(HttpServletRequest request, String state) {
        // Generate a CSRF token and store it in the in-memory cache.
        // Key = csrfToken, Value = original redirectUrl.
        // On callback, we validate that the same csrfToken is paired with the same redirectUrl.
        String csrfToken = RandomUtil.randomNumbers(32);
        requestCache.put(csrfToken, state);
        return buildGithubAuthUrl(state, csrfToken);
    }

    private String buildGithubAuthUrl(String redirectUrl, String csrfToken) {
        String redirectUri = buildCallbackUrl();
        StringBuilder url = new StringBuilder(GITHUB_AUTHORIZE_URL);
        url.append("?client_id=").append(clientId);
        url.append("&redirect_uri=").append(java.net.URLEncoder.encode(redirectUri, StandardCharsets.UTF_8));
        url.append("&scope=read:user,user:email");
        // state format: <original_redirect_url>_<csrf_token>
        url.append("&state=").append(redirectUrl).append("_").append(csrfToken);
        return url.toString();
    }

    /**
     * Validates the CSRF token in the callback state parameter.
     * Returns the original redirect URL if valid, throws BusinessException if not.
     */
    @Override
    public String validateAndExtractRedirectUrl(String state) {
        if (StringUtils.isBlank(state) || !state.contains("_")) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Invalid OAuth state");
        }
        int lastUnderscore = state.lastIndexOf('_');
        String redirectUrl = state.substring(0, lastUnderscore);
        String csrfToken = state.substring(lastUnderscore + 1);
        String storedRedirect = requestCache.get(csrfToken);
        if (storedRedirect == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OAuth state expired or tampered");
        }
        if (!storedRedirect.equals(redirectUrl)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "OAuth state mismatch");
        }
        requestCache.remove(csrfToken);
        return redirectUrl;
    }

    /**
     * Extracts just the redirect URL portion from the state parameter for error cases.
     * Does NOT validate — just parses the URL before the last underscore.
     */
    @Override
    public String extractRedirectUrlFromState(String state) {
        if (StringUtils.isBlank(state) || !state.contains("_")) {
            return "/";
        }
        int lastUnderscore = state.lastIndexOf('_');
        return state.substring(0, lastUnderscore);
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
            resultVO.setUserAccount(existingUser.getUserAccount());
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, existingUser);
        } else if (loginUser != null) {
            // Logged in user — bind GitHub account
            loginUser.setGithubId(githubId);
            if (StringUtils.isBlank(loginUser.getUserAvatar())) {
                loginUser.setUserAvatar(githubAvatar);
            }
            userService.updateById(loginUser);
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUser);
            resultVO = userService.getLoginUserVO(loginUser);
        } else {
            // New user — auto-register
            User newUser = createGithubUser(githubId, githubLogin, githubAvatar);
            String token = jwtUtils.generateToken(newUser.getId(), newUser.getUserAccount());
            resultVO = userService.getLoginUserVO(newUser);
            resultVO.setToken(token);
            // newUser.getUserAccount() is "github_" + githubLogin — set it directly so it's available in callback.
            resultVO.setUserAccount(newUser.getUserAccount());
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
        boolean updated = userService.updateById(loginUser);
        if (updated) {
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, loginUser);
            User refreshed = userService.getById(loginUser.getId());
            return refreshed != null;
        }
        return false;
    }

    @Override
    public LoginUserVO unbindGithubAccount(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // Use LambdaUpdateWrapper to force-set githubId to null.
        // updateById skips null fields by default, so we need a wrapper.
        userService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>(new User())
                .set(User::getGithubId, null)
                .eq(User::getId, loginUser.getId()));
        User updated = userService.getById(loginUser.getId());
        return userService.getLoginUserVO(updated);
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
        String host = getBackendHost();
        if (host.startsWith("http://") || host.startsWith("https://")) {
            host = host.replaceFirst("https?://", "");
        }
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return "https://" + host + "/api/oauth/github/callback";
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
        String response;
        try {
            response = restTemplate.postForObject(GITHUB_TOKEN_URL, entity, String.class);
        } catch (HttpClientErrorException e) {
            log.error("GitHub token exchange failed: {}", e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to exchange GitHub code for token: " + e.getResponseBodyAsString());
        }

        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "GitHub token exchange returned empty response");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> tokenResponse = mapper.readValue(response,
                    new TypeReference<Map<String, String>>() {});
            String token = tokenResponse.get("access_token");
            if (StringUtils.isBlank(token)) {
                String error = tokenResponse.get("error_description");
                log.error("GitHub token error: {}", error);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                        "Failed to exchange GitHub code for token: " + (error != null ? error : "unknown error"));
            }
            return token;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse GitHub token response: {}", response, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to parse GitHub token response");
        }
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
        // GitHub users have no password; use a hashed placeholder so the field is not null.
        String hashedPassword = DigestUtil.md5Hex(SALT + githubId + "github_oauth");
        String githubAvatarUrl = (githubAvatar != null && !githubAvatar.isEmpty())
                ? githubAvatar
                : "https://image-bed-ichensw.oss-cn-hangzhou.aliyuncs.com/Multiavatar-f5871c303317a4dafbf6.png";

        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(hashedPassword);
        user.setUserName(githubLogin);
        user.setUserAvatar(githubAvatarUrl);
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
