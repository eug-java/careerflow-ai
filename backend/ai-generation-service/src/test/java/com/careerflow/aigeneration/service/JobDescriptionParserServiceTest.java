package com.careerflow.aigeneration.service;

import com.careerflow.aigeneration.dto.ParsedJobDescriptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JobDescriptionParserServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void parseShouldReturnStructuredJobDescriptionFromAiJson() {
        UserChatClientFactory chatClientFactory = mock(UserChatClientFactory.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientFactory.forUser(USER_ID)).thenReturn(chatClient);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("""
                        {
                          "title": "Senior Java Engineer",
                          "companyName": "CareerFlow",
                          "location": "Austin, TX",
                          "employmentType": "Full-time",
                          "salaryMin": 120000,
                          "salaryMax": 150000,
                          "currency": "USD",
                          "remote": true,
                          "description": "Build scalable backend services.",
                          "skills": [
                            {"name": "Java", "required": true},
                            {"name": "Kafka", "required": false}
                          ]
                        }
                        """);
        JobDescriptionParserService service = new JobDescriptionParserService(chatClientFactory, new ObjectMapper());

        ParsedJobDescriptionResponse response = service.parse(USER_ID, "raw job description");

        assertThat(response.title()).isEqualTo("Senior Java Engineer");
        assertThat(response.companyName()).isEqualTo("CareerFlow");
        assertThat(response.location()).isEqualTo("Austin, TX");
        assertThat(response.salaryMin()).isEqualTo(120000.0);
        assertThat(response.salaryMax()).isEqualTo(150000.0);
        assertThat(response.remote()).isTrue();
        assertThat(response.skills())
                .hasSize(2)
                .extracting("name")
                .containsExactly("Java", "Kafka");
        assertThat(response.skills().getFirst().required()).isTrue();
        assertThat(response.skills().get(1).required()).isFalse();
    }

    @Test
    void parseShouldThrowIllegalStateExceptionWhenAiReturnsInvalidJson() {
        UserChatClientFactory chatClientFactory = mock(UserChatClientFactory.class);
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientFactory.forUser(any())).thenReturn(chatClient);
        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("not valid json");
        JobDescriptionParserService service = new JobDescriptionParserService(chatClientFactory, new ObjectMapper());

        assertThatThrownBy(() -> service.parse(USER_ID, "raw job description"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Failed to parse job description with AI")
                .hasCauseInstanceOf(Exception.class);
    }
}
