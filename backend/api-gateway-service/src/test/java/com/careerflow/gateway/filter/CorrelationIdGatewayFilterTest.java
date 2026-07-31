package com.careerflow.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdGatewayFilterTest {

    private final CorrelationIdGatewayFilter filter = new CorrelationIdGatewayFilter();

    @Test
    void filterAddsCorrelationIdWhenMissing() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/api/v1/profiles").build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdGatewayFilter.HEADER)).isNotBlank();
    }

    @Test
    void filterPreservesExistingCorrelationId() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("http://localhost/api/v1/profiles")
                        .header(CorrelationIdGatewayFilter.HEADER, "corr-999")
                        .build()
        );
        GatewayFilterChain chain = serverWebExchange -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

        assertThat(exchange.getResponse().getHeaders().getFirst(CorrelationIdGatewayFilter.HEADER)).isEqualTo("corr-999");
    }
}
