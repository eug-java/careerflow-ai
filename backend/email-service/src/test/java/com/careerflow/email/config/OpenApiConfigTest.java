package com.careerflow.email.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void openApiShouldExposeExpectedMetadata() {
        OpenAPI openAPI = new OpenApiConfig().emailOpenApi();

        assertThat(openAPI.getInfo()).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CareerFlow Email Service API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("IMAP/SMTP integration for recruiter email tracking and replies");
    }
}
