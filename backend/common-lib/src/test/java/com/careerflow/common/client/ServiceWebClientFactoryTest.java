package com.careerflow.common.client;

import com.careerflow.common.client.InternalClientHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ServiceWebClientFactoryTest {

    private ServiceWebClientFactory factory;

    @BeforeEach
    void setUp() {
        factory = new ServiceWebClientFactory(new InternalClientHeaders("internal-key"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void serviceAuthFilterShouldForwardJwtForAuthenticatedUsers() {
        Jwt jwt = Jwt.withTokenValue("user-jwt")
                .header("alg", "none")
                .subject("demo")
                .claim("roles", List.of("USER"))
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        ExchangeFunction exchange = request -> {
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer user-jwt");
            assertThat(request.headers().getFirst(InternalClientHeaders.HEADER)).isNull();
            return Mono.empty();
        };

        factory.serviceAuthFilter()
                .filter(ClientRequest.create(org.springframework.http.HttpMethod.GET, java.net.URI.create("http://test")).build(), exchange)
                .block();
    }

    @Test
    void serviceAuthFilterShouldUseInternalKeyForInternalServiceCalls() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "internal-service",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                )
        );

        ExchangeFunction exchange = request -> {
            assertThat(request.headers().getFirst(InternalClientHeaders.HEADER)).isEqualTo("internal-key");
            assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isNull();
            return Mono.empty();
        };

        factory.serviceAuthFilter()
                .filter(ClientRequest.create(org.springframework.http.HttpMethod.GET, java.net.URI.create("http://test")).build(), exchange)
                .block();
    }
}
