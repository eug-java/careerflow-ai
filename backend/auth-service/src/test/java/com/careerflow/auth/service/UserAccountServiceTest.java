package com.careerflow.auth.service;

import com.careerflow.auth.dto.RegisterRequest;
import com.careerflow.auth.entity.UserAccountEntity;
import com.careerflow.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    private UserAccountService service;

    @BeforeEach
    void setUp() {
        service = new UserAccountService(userAccountRepository, new MockEnvironment());
    }

    @Test
    void authenticateReturnsUserIdForValidCredentials() {
        UUID userId = UUID.nameUUIDFromBytes("careerflow-user:demo".getBytes());
        String passwordHash = new BCryptPasswordEncoder().encode("demo");
        when(userAccountRepository.findByUsername("demo"))
                .thenReturn(Optional.of(new UserAccountEntity(userId, "demo", passwordHash)));

        UUID actual = service.authenticate("demo", "demo");

        assertThat(actual).isEqualTo(userId);
    }

    @Test
    void authenticateThrowsForInvalidPassword() {
        UUID userId = UUID.randomUUID();
        String passwordHash = new BCryptPasswordEncoder().encode("demo");
        when(userAccountRepository.findByUsername("demo"))
                .thenReturn(Optional.of(new UserAccountEntity(userId, "demo", passwordHash)));

        assertThatThrownBy(() -> service.authenticate("demo", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void authenticateThrowsForUnknownUser() {
        when(userAccountRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate("unknown", "demo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void registerCreatesUserWithStableUserId() {
        when(userAccountRepository.existsByUsername("newuser")).thenReturn(false);
        when(userAccountRepository.save(any(UserAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UUID userId = service.register(new RegisterRequest("newuser", "password123"));

        assertThat(userId).isEqualTo(UUID.nameUUIDFromBytes("careerflow-user:newuser".getBytes()));
        ArgumentCaptor<UserAccountEntity> captor = ArgumentCaptor.forClass(UserAccountEntity.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("newuser");
        assertThat(new BCryptPasswordEncoder().matches("password123", captor.getValue().getPasswordHash())).isTrue();
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        when(userAccountRepository.existsByUsername("demo")).thenReturn(true);

        assertThatThrownBy(() -> service.register(new RegisterRequest("demo", "password123")))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");

        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void seedDefaultUsersIfMissingCreatesDemoUserInDev() {
        when(userAccountRepository.existsByUsername("demo")).thenReturn(false);
        when(userAccountRepository.save(any(UserAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.seedDefaultUsersIfMissing();

        ArgumentCaptor<UserAccountEntity> captor = ArgumentCaptor.forClass(UserAccountEntity.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("demo");
        assertThat(captor.getValue().getUserId())
                .isEqualTo(UUID.nameUUIDFromBytes("careerflow-user:demo".getBytes()));
    }

    @Test
    void seedDefaultUsersIfMissingCreatesAdminUserInProd() {
        UserAccountService prodService = new UserAccountService(
                userAccountRepository,
                new MockEnvironment().withProperty("spring.profiles.active", "prod")
        );
        when(userAccountRepository.existsByUsername("admin")).thenReturn(false);
        when(userAccountRepository.save(any(UserAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        prodService.seedDefaultUsersIfMissing();

        ArgumentCaptor<UserAccountEntity> captor = ArgumentCaptor.forClass(UserAccountEntity.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("admin");
        assertThat(captor.getValue().getUserId())
                .isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}
