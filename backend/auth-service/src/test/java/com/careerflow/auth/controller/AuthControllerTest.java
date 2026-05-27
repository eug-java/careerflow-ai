package com.careerflow.auth.controller;

import com.careerflow.auth.dto.LoginRequest;
import com.careerflow.auth.dto.LoginResponse;
import com.careerflow.auth.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthController authController;

    @Test
    void loginShouldReturnBearerTokenForValidDemoCredentials() {
        when(jwtTokenService.generateToken("demo")).thenReturn("jwt-token");
        when(jwtTokenService.expiresInSeconds()).thenReturn(7200L);

        LoginResponse response = authController.login(new LoginRequest("demo", "demo"));

        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(7200L);
        verify(jwtTokenService).generateToken("demo");
        verify(jwtTokenService).expiresInSeconds();
    }

    @Test
    void loginShouldThrowUnauthorizedWhenUsernameIsInvalid() {
        assertThatThrownBy(() -> authController.login(new LoginRequest("wrong", "demo")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getReason()).isEqualTo("Invalid credentials");
                });

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void loginShouldThrowUnauthorizedWhenPasswordIsInvalid() {
        assertThatThrownBy(() -> authController.login(new LoginRequest("demo", "wrong")))
                .isInstanceOfSatisfying(ResponseStatusException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(ex.getReason()).isEqualTo("Invalid credentials");
                });

        verifyNoInteractions(jwtTokenService);
    }
}
