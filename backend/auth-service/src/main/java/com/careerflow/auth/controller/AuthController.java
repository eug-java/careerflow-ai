/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.auth.controller;

import com.careerflow.auth.dto.LoginRequest;
import com.careerflow.auth.dto.LoginResponse;
import com.careerflow.auth.dto.RefreshTokenRequest;
import com.careerflow.auth.security.JwtTokenService;
import com.careerflow.auth.security.RefreshTokenService;
import com.careerflow.auth.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserAccountService userAccountService;

    public AuthController(
            JwtTokenService jwtTokenService,
            RefreshTokenService refreshTokenService,
            UserAccountService userAccountService
    ) {
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.userAccountService = userAccountService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        UUID userId;
        try {
            userId = userAccountService.authenticate(request.username(), request.password());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
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
            String accessToken = refreshTokenService.refreshAccessToken(request.refreshToken());
            return new LoginResponse(accessToken, request.refreshToken(), "Bearer", jwtTokenService.expiresInSeconds());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, ex.getMessage());
        }
    }
}
