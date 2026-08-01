package com.careerflow.aigeneration.service;

import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.aigeneration.dto.AiAccountResponse;
import com.careerflow.aigeneration.dto.AiConnectionTestResponse;
import com.careerflow.aigeneration.dto.UpsertAiAccountRequest;
import com.careerflow.aigeneration.entity.AiCredentialEntity;
import com.careerflow.aigeneration.repository.AiCredentialRepository;
import com.careerflow.aigeneration.security.CredentialEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AiCredentialService {

    private static final String DEFAULT_PROVIDER = "openai";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private final AiCredentialRepository credentialRepository;
    private final CredentialEncryptor credentialEncryptor;
    private final UserChatClientFactory chatClientFactory;

    public AiCredentialService(
            AiCredentialRepository credentialRepository,
            CredentialEncryptor credentialEncryptor,
            UserChatClientFactory chatClientFactory
    ) {
        this.credentialRepository = credentialRepository;
        this.credentialEncryptor = credentialEncryptor;
        this.chatClientFactory = chatClientFactory;
    }

    @Transactional(readOnly = true)
    public AiAccountResponse getAccount() {
        UUID ownerId = CurrentUserProvider.requireUserId();
        return credentialRepository.findByOwnerId(ownerId)
                .map(this::toResponse)
                .orElse(new AiAccountResponse(null, DEFAULT_MODEL, null, null, false));
    }

    @Transactional
    public AiAccountResponse upsertAccount(UpsertAiAccountRequest request) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        String provider = normalizeProvider(request.provider());
        String model = normalizeModel(request.preferredModel());
        String encryptedApiKey = credentialEncryptor.encrypt(request.apiKey().trim());

        AiCredentialEntity entity = credentialRepository.findByOwnerId(ownerId)
                .map(existing -> {
                    existing.update(provider, encryptedApiKey, model);
                    return existing;
                })
                .orElseGet(() -> new AiCredentialEntity(
                        UUID.randomUUID(),
                        ownerId,
                        provider,
                        encryptedApiKey,
                        model
                ));

        return toResponse(credentialRepository.save(entity));
    }

    @Transactional
    public void deleteAccount() {
        UUID ownerId = CurrentUserProvider.requireUserId();
        credentialRepository.findByOwnerId(ownerId).ifPresent(credentialRepository::delete);
    }

    public AiConnectionTestResponse testConnection(UpsertAiAccountRequest request) {
        String model = normalizeModel(request.preferredModel());
        try {
            chatClientFactory.forApiKey(request.apiKey().trim(), model)
                    .prompt()
                    .user("Reply with OK")
                    .call()
                    .content();
            return new AiConnectionTestResponse(true, "OpenAI API key is valid");
        } catch (Exception ex) {
            return new AiConnectionTestResponse(false, ex.getMessage());
        }
    }

    private AiAccountResponse toResponse(AiCredentialEntity entity) {
        return new AiAccountResponse(
                entity.getProvider(),
                entity.getPreferredModel(),
                maskApiKey(credentialEncryptor.decrypt(entity.getEncryptedApiKey())),
                entity.getUpdatedAt(),
                true
        );
    }

    private String normalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            return DEFAULT_PROVIDER;
        }
        return provider.trim().toLowerCase();
    }

    private String normalizeModel(String model) {
        if (model == null || model.isBlank()) {
            return DEFAULT_MODEL;
        }
        return model.trim();
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "****";
        }
        return apiKey.substring(0, 3) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}
