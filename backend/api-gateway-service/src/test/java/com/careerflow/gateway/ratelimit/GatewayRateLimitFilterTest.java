package com.careerflow.gateway.ratelimit;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRateLimitFilterTest {

    private final GatewayRateLimiter rateLimiter = new GatewayRateLimiter();
    private final GatewayRateLimitFilter filter = new GatewayRateLimitFilter(rateLimiter);

    @Test
    void allowsRequestsBelowLoginLimit() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("http://localhost/api/v1/auth/login").build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void blocksRequestsAboveLoginLimit() {
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();
        HttpStatusCode lastStatus = null;

        for (int attempt = 0; attempt < 21; attempt++) {
            MockServerWebExchange exchange = MockServerWebExchange.from(
                    MockServerHttpRequest.post("http://localhost/api/v1/auth/login").build()
            );
            StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();
            lastStatus = exchange.getResponse().getStatusCode();
        }

        assertThat(lastStatus).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void ignoresNonRateLimitedRoutes() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/api/v1/profiles").build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }
}
