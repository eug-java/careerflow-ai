package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.entity.AiCredentialEntity;
import com.careerflow.aigeneration.repository.AiCredentialRepository;
import com.careerflow.aigeneration.security.CredentialEncryptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserChatClientFactory {

    private static final String DEFAULT_MODEL = "gpt-4o-mini";
    private static final double DEFAULT_TEMPERATURE = 0.2;

    private final AiCredentialRepository credentialRepository;
    private final CredentialEncryptor credentialEncryptor;
    private final String fallbackApiKey;

    public UserChatClientFactory(
            AiCredentialRepository credentialRepository,
            CredentialEncryptor credentialEncryptor,
            @Value("${careerflow.ai.fallback-api-key:}") String fallbackApiKey
    ) {
        this.credentialRepository = credentialRepository;
        this.credentialEncryptor = credentialEncryptor;
        this.fallbackApiKey = fallbackApiKey;
    }

    public ChatClient forUser(UUID userId) {
        ResolvedCredentials credentials = resolveCredentials(userId);
        return buildClient(credentials.apiKey(), credentials.model());
    }

    public ChatClient forApiKey(String apiKey, String model) {
        return buildClient(apiKey, normalizeModel(model));
    }

    public ResolvedCredentials resolveCredentials(UUID userId) {
        Optional<AiCredentialEntity> stored = credentialRepository.findByOwnerId(userId);
        if (stored.isPresent()) {
            AiCredentialEntity entity = stored.get();
            return new ResolvedCredentials(
                    credentialEncryptor.decrypt(entity.getEncryptedApiKey()),
                    normalizeModel(entity.getPreferredModel())
            );
        }
        if (fallbackApiKey != null && !fallbackApiKey.isBlank()) {
            return new ResolvedCredentials(fallbackApiKey, DEFAULT_MODEL);
        }
        throw new AiCredentialsNotConfiguredException(
                "OpenAI API key is not configured. Add your key in AI Settings."
        );
    }

    public String resolveModel(UUID userId) {
        return credentialRepository.findByOwnerId(userId)
                .map(AiCredentialEntity::getPreferredModel)
                .map(this::normalizeModel)
                .orElse(DEFAULT_MODEL);
    }

    private ChatClient buildClient(String apiKey, String model) {
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .options(OpenAiChatOptions.builder()
                        .apiKey(apiKey)
                        .model(model)
                        .temperature(DEFAULT_TEMPERATURE)
                        .build())
                .build();
        return ChatClient.builder(chatModel).build();
    }

    private String normalizeModel(String model) {
        if (model == null || model.isBlank()) {
            return DEFAULT_MODEL;
        }
        return model.trim();
    }

    public record ResolvedCredentials(String apiKey, String model) {
    }
}
