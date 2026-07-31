package com.careerflow.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import static org.assertj.core.api.Assertions.assertThat;

class GatewaySecurityConfigTest {

    private final GatewaySecurityConfig config = new GatewaySecurityConfig();

    @Test
    void jwtDecoderIsCreated() {
        ReactiveJwtDecoder decoder = config.jwtDecoder("change-me-change-me-change-me-change-me");

        assertThat(decoder).isNotNull();
    }
}
