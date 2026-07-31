package com.careerflow.common.test;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.List;
import java.util.UUID;

public final class TestAuthSupport {

    private TestAuthSupport() {
    }

    public static UUID authenticateTestUser() {
        UUID userId = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .subject("demo")
                .claim("userId", userId.toString())
                .claim("roles", List.of("USER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        return userId;
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }
}
