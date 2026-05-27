/*************************************
 * SPDX-License-Identifier: MIT
 * Copyright (c) 2026 Evgenii Buianov
 */

package com.careerflow.auth.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtTokenService {

    private final String secret;
    private final String issuer;
    private final long expirationMinutes;

    public JwtTokenService(
            @Value("${careerflow.jwt.secret}") String secret,
            @Value("${careerflow.jwt.issuer}") String issuer,
            @Value("${careerflow.jwt.expiration-minutes}") long expirationMinutes
    ) {
        this.secret = secret;
        this.issuer = issuer;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(expirationMinutes * 60);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .issuer(issuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("roles", List.of("USER"))
                    .build();

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);

            signedJWT.sign(new MACSigner(secret.getBytes()));

            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to generate JWT token", e);
        }
    }

    public long expiresInSeconds() {
        return expirationMinutes * 60;
    }
}
