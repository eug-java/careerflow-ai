package com.careerflow.common.security;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ResourceServerSecurityConfig.class, JwtSecretValidator.class, InternalApiKeyFilter.class})
public class CareerflowSecurityAutoConfiguration {
}
