package com.careerflow.email.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI emailOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("CareerFlow Email Service API")
                        .description("IMAP/SMTP integration for recruiter email tracking and replies")
                        .version("1.0.0"));
    }
}
