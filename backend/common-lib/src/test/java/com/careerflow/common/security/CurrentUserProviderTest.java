package com.careerflow.common.security;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.test.TestAuthSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurrentUserProviderTest {

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void requireCurrentUserReturnsUserFromJwt() {
        UUID userId = TestAuthSupport.authenticateTestUser();

        CurrentUser currentUser = CurrentUserProvider.requireCurrentUser();

        assertThat(currentUser.userId()).isEqualTo(userId);
        assertThat(currentUser.username()).isEqualTo("demo");
        assertThat(CurrentUserProvider.requireUserId()).isEqualTo(userId);
    }

    @Test
    void requireCurrentUserThrowsWhenAuthenticationIsMissing() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(CurrentUserProvider::requireCurrentUser)
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Authenticated user is required");
    }

    @Test
    void requireCurrentUserThrowsWhenUserIdClaimIsMissing() {
        org.springframework.security.oauth2.jwt.Jwt jwt = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("test")
                .header("alg", "none")
                .subject("demo")
                .claim("roles", List.of("USER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(jwt)
        );

        assertThatThrownBy(CurrentUserProvider::requireCurrentUser)
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("JWT userId claim is required");
    }
}
