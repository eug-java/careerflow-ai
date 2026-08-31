package com.careerflow.auth.service;

import com.careerflow.auth.config.GitHubOAuthProperties;
import com.careerflow.auth.dto.LoginResponse;
import com.careerflow.auth.entity.OAuthIdentityEntity;
import com.careerflow.auth.entity.UserAccountEntity;
import com.careerflow.auth.repository.OAuthIdentityRepository;
import com.careerflow.auth.repository.UserAccountRepository;
import com.careerflow.auth.security.JwtTokenService;
import com.careerflow.auth.security.RefreshTokenService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Optional;
import java.util.UUID;

@Service
public class GitHubOAuthService {

    private static final String PROVIDER = "github";

    private final GitHubOAuthProperties properties;
    private final OAuthIdentityRepository oauthIdentityRepository;
    private final UserAccountRepository userAccountRepository;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final RestClient githubClient;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public GitHubOAuthService(
            GitHubOAuthProperties properties,
            OAuthIdentityRepository oauthIdentityRepository,
            UserAccountRepository userAccountRepository,
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService
    ) {
        this.properties = properties;
        this.oauthIdentityRepository = oauthIdentityRepository;
        this.userAccountRepository = userAccountRepository;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.githubClient = RestClient.builder()
                .baseUrl("https://github.com")
                .build();
    }

    public boolean isEnabled() {
        return properties.isConfigured();
    }

    @Transactional
    public LoginResponse authenticate(String code) {
        if (!isEnabled()) {
            throw new IllegalStateException("GitHub OAuth is not configured");
        }

        GitHubAccessTokenResponse tokenResponse = exchangeCode(code);
        GitHubUserResponse githubUser = fetchUser(tokenResponse.accessToken());
        UUID userId = resolveUserId(githubUser);
        String username = resolveUsername(githubUser);

        String accessToken = jwtTokenService.generateToken(username, userId);
        String refreshToken = refreshTokenService.issueRefreshToken(username, userId);
        return new LoginResponse(accessToken, refreshToken, "Bearer", jwtTokenService.expiresInSeconds());
    }

    private GitHubAccessTokenResponse exchangeCode(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        form.add("code", code);
        form.add("redirect_uri", properties.redirectUri());

        return githubClient.post()
                .uri("/login/oauth/access_token")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GitHubAccessTokenResponse.class);
    }

    private GitHubUserResponse fetchUser(String accessToken) {
        return RestClient.create()
                .get()
                .uri("https://api.github.com/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .retrieve()
                .body(GitHubUserResponse.class);
    }

    private UUID resolveUserId(GitHubUserResponse githubUser) {
        Optional<OAuthIdentityEntity> existingIdentity = oauthIdentityRepository.findByProviderAndProviderSubject(
                PROVIDER,
                String.valueOf(githubUser.id())
        );
        if (existingIdentity.isPresent()) {
            return existingIdentity.get().getUserId();
        }

        String username = resolveUsername(githubUser);
        Optional<UserAccountEntity> existingAccount = userAccountRepository.findByUsername(username);
        UUID userId = existingAccount.map(UserAccountEntity::getUserId)
                .orElseGet(() -> createOAuthUser(username));

        oauthIdentityRepository.save(new OAuthIdentityEntity(PROVIDER, String.valueOf(githubUser.id()), userId));
        return userId;
    }

    private UUID createOAuthUser(String username) {
        UUID userId = UUID.nameUUIDFromBytes(("careerflow-user:" + username).getBytes());
        userAccountRepository.save(new UserAccountEntity(
                userId,
                username,
                passwordEncoder.encode(UUID.randomUUID().toString())
        ));
        return userId;
    }

    private String resolveUsername(GitHubUserResponse githubUser) {
        String candidate = githubUser.login() == null ? "github_user" : githubUser.login();
        candidate = candidate.replaceAll("[^a-zA-Z0-9_]", "_").toLowerCase();
        if (candidate.length() < 3) {
            candidate = "gh_" + githubUser.id();
        }
        if (candidate.length() > 50) {
            candidate = candidate.substring(0, 50);
        }
        return candidate;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubAccessTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GitHubUserResponse(
            long id,
            String login
    ) {
    }
}
