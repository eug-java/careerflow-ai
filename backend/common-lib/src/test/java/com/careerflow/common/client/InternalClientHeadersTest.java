package com.careerflow.common.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InternalClientHeadersTest {

    @Test
    void apiKeyReturnsConfiguredValue() {
        InternalClientHeaders headers = new InternalClientHeaders("my-internal-key");

        assertThat(headers.apiKey()).isEqualTo("my-internal-key");
        assertThat(InternalClientHeaders.HEADER).isEqualTo("X-Careerflow-Internal-Key");
    }
}
