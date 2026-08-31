package com.careerflow.auth.security;

import com.careerflow.auth.entity.RefreshTokenEntity;
import com.careerflow.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private RefreshTokenRepository repository;
    private JwtTokenService jwtTokenService;
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        repository = mock(RefreshTokenRepository.class);
        jwtTokenService = mock(JwtTokenService.class);
        refreshTokenService = new RefreshTokenService(repository, jwtTokenService);
    }

    @Test
    void issueRefreshTokenPersistsEntity() {
        UUID userId = UUID.randomUUID();

        String refreshToken = refreshTokenService.issueRefreshToken("demo", userId);

        assertThat(refreshToken).isNotBlank();
        verify(repository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void rotateRefreshTokenIssuesNewTokensAndRevokesOldOne() {
        UUID userId = UUID.randomUUID();
        RefreshTokenEntity entity = new RefreshTokenEntity(
                "refresh-token",
                "demo",
                userId,
                Instant.now().plusSeconds(3600)
        );
        when(repository.findById("refresh-token")).thenReturn(Optional.of(entity));
        when(jwtTokenService.generateToken("demo", userId)).thenReturn("new-access-token");

        RefreshTokenRotationResult result = refreshTokenService.rotateRefreshToken("refresh-token");

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isNotBlank().isNotEqualTo("refresh-token");

        var order = inOrder(repository);
        order.verify(repository).deleteById("refresh-token");
        order.verify(repository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void rotateRefreshTokenThrowsForUnknownToken() {
        when(repository.findById("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired refresh token");
    }

    @Test
    void rotateRefreshTokenThrowsForExpiredToken() {
        RefreshTokenEntity entity = new RefreshTokenEntity(
                "expired-token",
                "demo",
                UUID.randomUUID(),
                Instant.now().minusSeconds(60)
        );
        when(repository.findById("expired-token")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("expired-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired refresh token");

        verify(repository).deleteById("expired-token");
    }

    @Test
    void purgeExpiredRefreshTokensDelegatesToRepository() {
        refreshTokenService.purgeExpiredRefreshTokens();

        verify(repository).deleteByExpiresAtBefore(any(Instant.class));
    }
}
