package com.careerflow.auth.security;

import com.careerflow.auth.entity.RefreshTokenEntity;
import com.careerflow.auth.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtTokenService jwtTokenService;

    public RefreshTokenService(RefreshTokenRepository repository, JwtTokenService jwtTokenService) {
        this.repository = repository;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public String issueRefreshToken(String username, UUID userId) {
        String refreshToken = UUID.randomUUID().toString();
        repository.save(new RefreshTokenEntity(
                refreshToken,
                username,
                userId,
                Instant.now().plusSeconds(7 * 24 * 3600)
        ));
        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        repository.deleteById(refreshToken);
    }

    @Transactional
    public RefreshTokenRotationResult rotateRefreshToken(String refreshToken) {
        RefreshTokenEntity entity = repository.findById(refreshToken).orElse(null);
        if (entity == null || entity.getExpiresAt().isBefore(Instant.now())) {
            if (entity != null) {
                repository.deleteById(refreshToken);
            }
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        repository.deleteById(refreshToken);
        String newRefreshToken = issueRefreshToken(entity.getUsername(), entity.getUserId());
        String accessToken = jwtTokenService.generateToken(entity.getUsername(), entity.getUserId());
        return new RefreshTokenRotationResult(accessToken, newRefreshToken);
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredRefreshTokens() {
        repository.deleteByExpiresAtBefore(Instant.now());
    }
}
