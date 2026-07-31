package com.careerflow.auth.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAccountServiceTest {

    @Test
    void authenticateReturnsUserIdForDemoCredentials() {
        UserAccountService service = new UserAccountService(new MockEnvironment());

        UUID userId = service.authenticate("demo", "demo");

        assertThat(userId).isEqualTo(UUID.nameUUIDFromBytes("careerflow-user:demo".getBytes()));
    }

    @Test
    void authenticateThrowsForInvalidPassword() {
        UserAccountService service = new UserAccountService(new MockEnvironment());

        assertThatThrownBy(() -> service.authenticate("demo", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void authenticateThrowsForUnknownUser() {
        UserAccountService service = new UserAccountService(new MockEnvironment());

        assertThatThrownBy(() -> service.authenticate("unknown", "demo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void prodProfileUsesAdminAccount() {
        MockEnvironment environment = new MockEnvironment().withProperty("spring.profiles.active", "prod");
        UserAccountService service = new UserAccountService(environment);

        UUID userId = service.authenticate("admin", "ChangeMeNow123!");

        assertThat(userId).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}
