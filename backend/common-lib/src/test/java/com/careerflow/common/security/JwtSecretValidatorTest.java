package com.careerflow.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtSecretValidatorTest {

    @Test
    void validateAllowsDefaultSecretOutsideProd() {
        MockEnvironment environment = new MockEnvironment();
        JwtSecretValidator validator = new JwtSecretValidator(
                environment,
                "change-me-change-me-change-me-change-me"
        );

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }

    @Test
    void validateRejectsDefaultSecretInProd() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        JwtSecretValidator validator = new JwtSecretValidator(
                environment,
                "change-me-change-me-change-me-change-me"
        );

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Production requires a strong JWT_SECRET");
    }

    @Test
    void validateRejectsShortSecretInProd() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        JwtSecretValidator validator = new JwtSecretValidator(environment, "short-secret");

        assertThatThrownBy(validator::validate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("min 32 chars");
    }

    @Test
    void validateAcceptsStrongSecretInProd() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        JwtSecretValidator validator = new JwtSecretValidator(
                environment,
                "this-is-a-very-strong-production-secret-key"
        );

        assertThatCode(validator::validate).doesNotThrowAnyException();
    }
}
