package com.careerflow.common.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InternalAuthSupportTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isInternalCallReturnsTrueForInternalRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                )
        );

        assertThat(InternalAuthSupport.isInternalCall()).isTrue();
    }

    @Test
    void isInternalCallReturnsFalseForUserRole() {
        com.careerflow.common.test.TestAuthSupport.authenticateTestUser();

        assertThat(InternalAuthSupport.isInternalCall()).isFalse();
    }

    @Test
    void isInternalCallReturnsFalseWhenUnauthenticated() {
        assertThat(InternalAuthSupport.isInternalCall()).isFalse();
    }
}
