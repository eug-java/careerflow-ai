package com.careerflow.auth.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    private final LoginRateLimiter limiter = new LoginRateLimiter();

    @Test
    void checkAllowedShouldRejectAfterTooManyAttempts() {
        for (int attempt = 0; attempt < 10; attempt++) {
            limiter.checkAllowed("demo");
        }

        assertThatThrownBy(() -> limiter.checkAllowed("demo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many login attempts");
    }
}
