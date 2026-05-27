package com.careerflow.matching.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceClientConfigTest {

    private final ServiceClientConfig config = new ServiceClientConfig();

    @Test
    void profileWebClientIsCreated() {
        WebClient client = config.profileWebClient("http://localhost:8081");

        assertThat(client).isNotNull();
    }

    @Test
    void jobWebClientIsCreated() {
        WebClient client = config.jobWebClient("http://localhost:8082");

        assertThat(client).isNotNull();
    }
}
