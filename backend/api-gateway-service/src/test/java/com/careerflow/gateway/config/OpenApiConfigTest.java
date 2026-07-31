package com.careerflow.gateway.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void openApiContainsGatewayMetadata() {
        var openAPI = new OpenApiConfig().openAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CareerFlow API gateway Service API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("0.1.0");
        assertThat(openAPI.getInfo().getDescription()).contains("API Gateway");
    }
}
