package com.careerflow.workflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

@Component
public class WorkflowJwtValidator {

    private final JwtDecoder jwtDecoder;

    public WorkflowJwtValidator(@Value("${careerflow.jwt.secret}") String secret) {
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        this.jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
    }

    public UUID validateAndExtractUserId(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String userId = jwt.getClaimAsString("userId");
            if (userId == null || userId.isBlank()) {
                throw new JwtException("Missing userId claim");
            }
            return UUID.fromString(userId);
        } catch (Exception ex) {
            throw new JwtException("Invalid access token", ex);
        }
    }
}
