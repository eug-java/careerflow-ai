package com.careerflow.auth.controller;

import com.careerflow.auth.dto.GitHubOAuthRequest;
import com.careerflow.auth.dto.LoginRequest;
import com.careerflow.auth.dto.LoginResponse;
import com.careerflow.auth.dto.RegisterRequest;
import com.careerflow.auth.security.JwtTokenService;
import com.careerflow.auth.security.LoginRateLimiter;
import com.careerflow.auth.security.RefreshTokenRotationResult;
import com.careerflow.auth.security.RefreshTokenService;
import com.careerflow.auth.service.GitHubOAuthService;
import com.careerflow.auth.service.UserAccountService;
import com.careerflow.auth.service.UsernameAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @Mock
    private GitHubOAuthService gitHubOAuthService;

    @InjectMocks
    private AuthController authController;

    @Test
    void githubOAuthEnabledShouldReflectServiceState() {
        when(gitHubOAuthService.isEnabled()).thenReturn(true);

        Map<String, Boolean> response = authController.githubOAuthEnabled();

        assertThat(response).containsEntry("enabled", true);
    }

    @Test
    void githubOAuthShouldReturnTokensWhenEnabled() {
        LoginResponse expected = new LoginResponse("jwt", "refresh", "Bearer", 7200L);
        when(gitHubOAuthService.isEnabled()).thenReturn(true);
        when(gitHubOAuthService.authenticate("auth-code")).thenReturn(expected);

        LoginResponse response = authController.githubOAuth(new GitHubOAuthRequest("auth-code"));

        assertThat(response).isEqualTo(expected);
    }

    @Test
    void githubOAuthShouldReturnNotFoundWhenDisabled() {
        when(gitHubOAuthService.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> authController.githubOAuth(new GitHubOAuthRequest("auth-code")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getReason()).isEqualTo("GitHub OAuth is not configured");
                });
    }

    @Test
    void registerShouldCreateAccountAndReturnTokens() {
        UUID userId = UUID.randomUUID();
        RegisterRequest request = new RegisterRequest("newuser", "password123");
        when(userAccountService.register(request)).thenReturn(userId);
        when(jwtTokenService.generateToken("newuser", userId)).thenReturn("jwt-token");
        when(refreshTokenService.issueRefreshToken("newuser", userId)).thenReturn("refresh-token");
        when(jwtTokenService.expiresInSeconds()).thenReturn(7200L);

        LoginResponse response = authController.register(request);

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(loginRateLimiter).checkAllowed("register:newuser");
    }

    @Test
    void registerShouldThrowConflictWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest("demo", "password123");
        when(userAccountService.register(request)).thenThrow(new UsernameAlreadyExistsException("Username already taken"));

        assertThatThrownBy(() -> authController.register(request))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getReason()).isEqualTo("Username already taken");
                });
    }

    @Test
    void loginShouldReturnBearerTokenForValidDemoCredentials() {
        UUID userId = UUID.randomUUID();
        when(userAccountService.authenticate("demo", "demo")).thenReturn(userId);
        when(jwtTokenService.generateToken("demo", userId)).thenReturn("jwt-token");
        when(refreshTokenService.issueRefreshToken("demo", userId)).thenReturn("refresh-token");
        when(jwtTokenService.expiresInSeconds()).thenReturn(7200L);

        LoginResponse response = authController.login(new LoginRequest("demo", "demo"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(7200L);
        verify(jwtTokenService).generateToken("demo", userId);
    }

    @Test
    void loginShouldThrowUnauthorizedWhenUsernameIsInvalid() {
        when(userAccountService.authenticate(eq("wrong"), any())).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> authController.login(new LoginRequest("wrong", "demo")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getReason()).isEqualTo("Invalid credentials");
                });

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void loginShouldThrowUnauthorizedWhenPasswordIsInvalid() {
        when(userAccountService.authenticate(eq("demo"), eq("wrong"))).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> authController.login(new LoginRequest("demo", "wrong")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getReason()).isEqualTo("Invalid credentials");
                });

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void refreshShouldReturnRotatedTokens() {
        when(refreshTokenService.rotateRefreshToken("refresh-token"))
                .thenReturn(new RefreshTokenRotationResult("new-jwt", "new-refresh-token"));
        when(jwtTokenService.expiresInSeconds()).thenReturn(7200L);

        LoginResponse response = authController.refresh(
                new com.careerflow.auth.dto.RefreshTokenRequest("refresh-token")
        );

        assertThat(response.accessToken()).isEqualTo("new-jwt");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(7200L);
    }

    @Test
    void refreshShouldThrowUnauthorizedForInvalidRefreshToken() {
        when(refreshTokenService.rotateRefreshToken("bad-token"))
                .thenThrow(new IllegalArgumentException("Invalid or expired refresh token"));

        assertThatThrownBy(() -> authController.refresh(
                new com.careerflow.auth.dto.RefreshTokenRequest("bad-token")
        )).isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getReason()).isEqualTo("Invalid or expired refresh token");
        });
    }

    @Test
    void logoutShouldRevokeRefreshToken() {
        authController.logout(new com.careerflow.auth.dto.RefreshTokenRequest("refresh-token"));

        verify(refreshTokenService).revokeRefreshToken("refresh-token");
    }
}
