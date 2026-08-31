/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.auth.controller;

import com.careerflow.auth.dto.GitHubOAuthRequest;
import com.careerflow.auth.dto.LoginRequest;
import com.careerflow.auth.dto.LoginResponse;
import com.careerflow.auth.dto.RefreshTokenRequest;
import com.careerflow.auth.dto.RegisterRequest;
import com.careerflow.auth.service.UsernameAlreadyExistsException;
import com.careerflow.auth.security.JwtTokenService;
import com.careerflow.auth.security.LoginRateLimiter;
import com.careerflow.auth.security.RefreshTokenRotationResult;
import com.careerflow.auth.security.RefreshTokenService;
import com.careerflow.auth.service.GitHubOAuthService;
import com.careerflow.auth.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserAccountService userAccountService;
    private final LoginRateLimiter loginRateLimiter;
    private final GitHubOAuthService gitHubOAuthService;

    public AuthController(
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            UserAccountService userAccountService,
            LoginRateLimiter loginRateLimiter,
            GitHubOAuthService gitHubOAuthService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.userAccountService = userAccountService;
        this.loginRateLimiter = loginRateLimiter;
        this.gitHubOAuthService = gitHubOAuthService;
    }

    @GetMapping("/oauth/github/enabled")
    public Map<String, Boolean> githubOAuthEnabled() {
        return Map.of("enabled", gitHubOAuthService.isEnabled());
    }

    @PostMapping("/oauth/github")
    public LoginResponse githubOAuth(@Valid @RequestBody GitHubOAuthRequest request) {
        if (!gitHubOAuthService.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GitHub OAuth is not configured");
        }
        try {
            return gitHubOAuthService.authenticate(request.code());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GitHub OAuth failed");
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@Valid @RequestBody RegisterRequest request) {
        loginRateLimiter.checkAllowed("register:" + request.username());
        UUID userId;
        try {
            userId = userAccountService.register(request);
        } catch (UsernameAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage());
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        }

        String accessToken = jwtTokenService.generateToken(request.username(), userId);
        String refreshToken = refreshTokenService.issueRefreshToken(request.username(), userId);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.expiresInSeconds()
        );
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        loginRateLimiter.checkAllowed(request.username());
        UUID userId;
        try {
            userId = userAccountService.authenticate(request.username(), request.password());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        } catch (IllegalStateException ex) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
        }

        String accessToken = jwtTokenService.generateToken(request.username(), userId);
        String refreshToken = refreshTokenService.issueRefreshToken(request.username(), userId);

        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                jwtTokenService.expiresInSeconds()
        );
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenRotationResult rotation = refreshTokenService.rotateRefreshToken(request.refreshToken());
            return new LoginResponse(
                    rotation.accessToken(),
                    rotation.refreshToken(),
                    "Bearer",
                    jwtTokenService.expiresInSeconds()
            );
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.refreshToken());
    }
}
