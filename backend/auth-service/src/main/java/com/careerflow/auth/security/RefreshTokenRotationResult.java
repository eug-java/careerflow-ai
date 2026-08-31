package com.careerflow.auth.security;

public record RefreshTokenRotationResult(String accessToken, String refreshToken) {
}
