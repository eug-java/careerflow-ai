package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.dto.AiAccountResponse;
import com.careerflow.aigeneration.dto.UpsertAiAccountRequest;
import com.careerflow.aigeneration.entity.AiCredentialEntity;
import com.careerflow.aigeneration.repository.AiCredentialRepository;
import com.careerflow.common.security.CredentialEncryptor;
import com.careerflow.common.test.TestAuthSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCredentialServiceTest {

    private AiCredentialRepository repository;
    private CredentialEncryptor encryptor;
    private UserChatClientFactory chatClientFactory;
    private AiCredentialService service;

    @BeforeEach
    void setUp() {
        repository = mock(AiCredentialRepository.class);
        encryptor = new CredentialEncryptor("0123456789abcdef0123456789abcdef");
        chatClientFactory = mock(UserChatClientFactory.class);
        service = new AiCredentialService(repository, encryptor, chatClientFactory);
    }

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void getAccountShouldReturnNotConfiguredWhenMissing() {
        UUID ownerId = TestAuthSupport.authenticateTestUser();
        when(repository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        AiAccountResponse response = service.getAccount();

        assertThat(response.configured()).isFalse();
        assertThat(response.preferredModel()).isEqualTo("gpt-4o-mini");
    }

    @Test
    void upsertAccountShouldEncryptAndPersistKey() {
        UUID ownerId = TestAuthSupport.authenticateTestUser();
        when(repository.findByOwnerId(ownerId)).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AiAccountResponse response = service.upsertAccount(
                new UpsertAiAccountRequest("sk-live-test-key", "openai", "gpt-4o-mini")
        );

        assertThat(response.configured()).isTrue();
        assertThat(response.apiKeyHint()).startsWith("sk-");
        assertThat(response.apiKeyHint()).endsWith("-key");
        verify(repository).save(any(AiCredentialEntity.class));
    }

    @Test
    void testConnectionShouldReturnSuccessWhenOpenAiResponds() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientFactory.forApiKey(anyString(), anyString())).thenReturn(chatClient);
        when(chatClient.prompt().user(anyString()).call().content()).thenReturn("OK");

        var response = service.testConnection(
                new UpsertAiAccountRequest("sk-live-test-key", "openai", "gpt-4o-mini")
        );

        assertThat(response.success()).isTrue();
    }
}
