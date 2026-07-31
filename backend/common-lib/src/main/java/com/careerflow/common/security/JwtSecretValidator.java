package com.careerflow.common.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class JwtSecretValidator {

    private static final String DEFAULT_SECRET = "change-me-change-me-change-me-change-me";

    private final Environment environment;
    private final String secret;

    public JwtSecretValidator(Environment environment,
                              @Value("${careerflow.jwt.secret}") String secret) {
        this.environment = environment;
        this.secret = secret;
    }

    @PostConstruct
    void validate() {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            return;
        }
        if (DEFAULT_SECRET.equals(secret) || secret.length() < 32) {
            throw new IllegalStateException("Production requires a strong JWT_SECRET (min 32 chars, not default)");
        }
    }
}
