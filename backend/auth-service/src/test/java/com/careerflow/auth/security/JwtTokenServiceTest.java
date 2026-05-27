package com.careerflow.auth.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-32";
    private static final String ISSUER = "careerflow-ai-test";

    @Test
    void generateTokenShouldReturnSignedJwtWithExpectedClaims() throws Exception {
        JwtTokenService service = new JwtTokenService(SECRET, ISSUER, 120);

        String token = service.generateToken("demo");

        SignedJWT jwt = SignedJWT.parse(token);
        assertThat(jwt.verify(new MACVerifier(SECRET.getBytes()))).isTrue();
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
        assertThat(jwt.getJWTClaimsSet().getSubject()).isEqualTo("demo");
        assertThat(jwt.getJWTClaimsSet().getIssuer()).isEqualTo(ISSUER);
        assertThat(jwt.getJWTClaimsSet().getStringListClaim("roles")).isEqualTo(List.of("USER"));
        assertThat(jwt.getJWTClaimsSet().getIssueTime()).isNotNull();
        assertThat(jwt.getJWTClaimsSet().getExpirationTime()).isNotNull();
        assertThat(jwt.getJWTClaimsSet().getExpirationTime().toInstant())
                .isAfter(Instant.now().plusSeconds(119 * 60))
                .isBefore(Instant.now().plusSeconds(121 * 60));
    }

    @Test
    void expiresInSecondsShouldConvertMinutesToSeconds() {
        JwtTokenService service = new JwtTokenService(SECRET, ISSUER, 15);

        assertThat(service.expiresInSeconds()).isEqualTo(900);
    }

    @Test
    void generateTokenShouldWrapJoseExceptionWhenSecretIsTooShort() {
        JwtTokenService service = new JwtTokenService("short-secret", ISSUER, 120);

        assertThatThrownBy(() -> service.generateToken("demo"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to generate JWT token");
    }
}
