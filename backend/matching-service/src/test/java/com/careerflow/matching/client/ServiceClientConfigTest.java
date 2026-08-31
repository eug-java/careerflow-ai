package com.careerflow.matching.client;

import com.careerflow.common.client.InternalClientHeaders;
import com.careerflow.common.client.ServiceWebClientFactory;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceClientConfigTest {

    private final ServiceClientConfig config = new ServiceClientConfig();
    private final ServiceWebClientFactory factory =
            new ServiceWebClientFactory(new InternalClientHeaders("test-internal-key"));

    @Test
    void profileWebClientIsCreated() {
        WebClient client = config.profileWebClient("http://localhost:8081", factory);

        assertThat(client).isNotNull();
    }

    @Test
    void jobWebClientIsCreated() {
        WebClient client = config.jobWebClient("http://localhost:8082", factory);

        assertThat(client).isNotNull();
    }
}
