package com.careerflow.auth.service;

import com.careerflow.auth.config.GitHubOAuthProperties;
import com.careerflow.auth.repository.OAuthIdentityRepository;
import com.careerflow.auth.repository.UserAccountRepository;
import com.careerflow.auth.security.JwtTokenService;
import com.careerflow.auth.security.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GitHubOAuthServiceTest {

    @Mock
    private OAuthIdentityRepository oauthIdentityRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private GitHubOAuthService createService(GitHubOAuthProperties properties) {
        return new GitHubOAuthService(
                properties,
                oauthIdentityRepository,
                userAccountRepository,
                jwtTokenService,
                refreshTokenService
        );
    }

    @Test
    void isEnabledShouldBeFalseWhenClientIdMissing() {
        GitHubOAuthService service = createService(
                new GitHubOAuthProperties("", "secret", "http://localhost/callback")
        );

        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void isEnabledShouldBeTrueWhenFullyConfigured() {
        GitHubOAuthService service = createService(
                new GitHubOAuthProperties("client-id", "client-secret", "http://localhost/callback")
        );

        assertThat(service.isEnabled()).isTrue();
    }

    @Test
    void authenticateShouldRejectWhenNotConfigured() {
        GitHubOAuthService service = createService(
                new GitHubOAuthProperties("", "secret", "http://localhost/callback")
        );

        assertThatThrownBy(() -> service.authenticate("code"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("GitHub OAuth is not configured");
    }
}
