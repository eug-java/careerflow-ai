package com.careerflow.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "careerflow.oauth.github")
public record GitHubOAuthProperties(
        String clientId,
        String clientSecret,
        String redirectUri
) {
    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank()
                && clientSecret != null && !clientSecret.isBlank()
                && redirectUri != null && !redirectUri.isBlank();
    }
}
