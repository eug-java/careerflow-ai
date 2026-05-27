
package com.careerflow.profile.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    @Test
    void profileServiceOpenAPIShouldContainExpectedInfo() {
        var openAPI = new OpenApiConfig().profileServiceOpenAPI();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("CareerFlow AI - Profile Service API");
        assertThat(openAPI.getInfo().getDescription())
                .isEqualTo("API for managing candidate profiles, skills, and professional experience.");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("0.1.0");
    }
}
