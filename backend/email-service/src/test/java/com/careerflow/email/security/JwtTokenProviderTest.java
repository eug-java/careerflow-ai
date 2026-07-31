package com.careerflow.email.security;

import com.careerflow.common.test.TestAuthSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider();

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void currentBearerTokenShouldReturnJwtValue() {
        TestAuthSupport.authenticateTestUser();

        assertThat(provider.currentBearerToken()).isEqualTo("test-token");
    }

    @Test
    void currentBearerTokenShouldFailWithoutAuthentication() {
        assertThatThrownBy(provider::currentBearerToken)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Authenticated JWT is required");
    }
}
