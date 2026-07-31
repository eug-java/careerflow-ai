package com.careerflow.auth.security;

import com.careerflow.auth.entity.RefreshTokenEntity;
import com.careerflow.auth.repository.RefreshTokenRepository;
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
    public String refreshAccessToken(String refreshToken) {
        RefreshTokenEntity entity = repository.findById(refreshToken).orElse(null);
        if (entity == null || entity.getExpiresAt().isBefore(Instant.now())) {
            repository.deleteById(refreshToken);
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }
        return jwtTokenService.generateToken(entity.getUsername(), entity.getUserId());
    }
}
