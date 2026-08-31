package com.careerflow.gateway.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayRateLimiterTest {

    private final GatewayRateLimiter limiter = new GatewayRateLimiter();

    @Test
    void allowsRequestsWithinLimit() {
        assertThat(limiter.tryConsume("client-1", 3, 60)).isTrue();
        assertThat(limiter.tryConsume("client-1", 3, 60)).isTrue();
        assertThat(limiter.tryConsume("client-1", 3, 60)).isTrue();
    }

    @Test
    void blocksRequestsAboveLimit() {
        assertThat(limiter.tryConsume("client-2", 2, 60)).isTrue();
        assertThat(limiter.tryConsume("client-2", 2, 60)).isTrue();
        assertThat(limiter.tryConsume("client-2", 2, 60)).isFalse();
    }

    @Test
    void tracksClientsIndependently() {
        assertThat(limiter.tryConsume("client-a", 1, 60)).isTrue();
        assertThat(limiter.tryConsume("client-a", 1, 60)).isFalse();
        assertThat(limiter.tryConsume("client-b", 1, 60)).isTrue();
    }
}
