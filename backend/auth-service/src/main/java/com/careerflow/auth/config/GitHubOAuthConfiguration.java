package com.careerflow.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GitHubOAuthProperties.class)
public class GitHubOAuthConfiguration {
}
