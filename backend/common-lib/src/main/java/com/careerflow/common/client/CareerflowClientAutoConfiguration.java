package com.careerflow.common.client;

import com.careerflow.common.security.CredentialEncryptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

@AutoConfiguration
@ConditionalOnClass(WebClient.class)
@Import({InternalClientHeaders.class, ServiceWebClientFactory.class})
public class CareerflowClientAutoConfiguration {

    @AutoConfiguration
    @ConditionalOnProperty("careerflow.credentials.encryption-key")
    @Import(CredentialEncryptor.class)
    static class CredentialEncryptorAutoConfiguration {
    }
}
