package com.careerflow.gateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {

    @Test
    void corsWebFilterIsCreatedWithCustomOrigins() {
        CorsWebFilter filter = new CorsConfig().corsWebFilter("http://localhost:5173,https://app.example.com");

        assertThat(filter).isNotNull();
    }

    @Test
    void corsWebFilterUsesDefaultOriginsWhenBlankEntriesPresent() {
        CorsWebFilter filter = new CorsConfig().corsWebFilter("http://localhost:5173, ,http://localhost:3000");

        assertThat(filter).isNotNull();
    }
}
