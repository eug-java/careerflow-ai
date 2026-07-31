package com.careerflow.workflow.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void openApiContainsWorkflowMetadata() {
        var openAPI = new OpenApiConfig().openAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CareerFlow Workflow Service API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("0.1.0");
        assertThat(openAPI.getInfo().getDescription()).contains("Camunda workflow");
    }
}
