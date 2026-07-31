package com.careerflow.email.controller;

import com.careerflow.common.api.GlobalExceptionHandler;
import com.careerflow.common.security.CareerflowSecurityAutoConfiguration;
import com.careerflow.email.dto.*;
import com.careerflow.email.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class, excludeAutoConfiguration = CareerflowSecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmailService emailService;

    @Test
    void getAccountShouldReturnConfiguredAccount() throws Exception {
        when(emailService.getAccount()).thenReturn(new EmailAccountResponse(
                "user@gmail.com", "imap.gmail.com", 993, "smtp.gmail.com", 587, true, Instant.now(), true
        ));

        mockMvc.perform(get("/api/v1/email/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("user@gmail.com"))
                .andExpect(jsonPath("$.configured").value(true));
    }

    @Test
    void upsertAccountShouldReturnSavedAccount() throws Exception {
        when(emailService.upsertAccount(any())).thenReturn(new EmailAccountResponse(
                "user@gmail.com", "imap.gmail.com", 993, "smtp.gmail.com", 587, true, Instant.now(), true
        ));

        mockMvc.perform(put("/api/v1/email/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accountRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailAddress").value("user@gmail.com"));

        verify(emailService).upsertAccount(any());
    }

    @Test
    void deleteAccountShouldReturnNoContent() throws Exception {
        doNothing().when(emailService).deleteAccount();

        mockMvc.perform(delete("/api/v1/email/account"))
                .andExpect(status().isNoContent());

        verify(emailService).deleteAccount();
    }

    @Test
    void syncInboxShouldReturnCounts() throws Exception {
        when(emailService.syncInbox()).thenReturn(new SyncEmailResponse(2, 5));

        mockMvc.perform(post("/api/v1/email/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedCount").value(2))
                .andExpect(jsonPath("$.totalInboxCount").value(5));
    }

    @Test
    void listMessagesShouldReturnInboxItems() throws Exception {
        UUID id = UUID.randomUUID();
        when(emailService.listMessages(EmailCategory.OFFER)).thenReturn(List.of(
                new InboxMessageResponse(
                        id,
                        "Offer",
                        "recruiter@example.com",
                        "user@gmail.com",
                        "Preview",
                        "Body",
                        Instant.parse("2026-07-01T10:00:00Z"),
                        EmailCategory.OFFER,
                        "offer",
                        null,
                        false
                )
        ));

        mockMvc.perform(get("/api/v1/email/messages").param("category", "OFFER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category").value("OFFER"));
    }

    @Test
    void summaryShouldReturnCategoryCounts() throws Exception {
        when(emailService.summary()).thenReturn(new EmailSummaryResponse(
                3,
                Map.of(EmailCategory.OFFER, 1L, EmailCategory.REJECTION, 2L),
                true
        ));

        mockMvc.perform(get("/api/v1/email/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(3))
                .andExpect(jsonPath("$.accountConfigured").value(true));
    }

    @Test
    void replyShouldReturnUpdatedMessage() throws Exception {
        UUID messageId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(emailService.reply(eq(messageId), any())).thenReturn(new InboxMessageResponse(
                messageId,
                "Re: Resume",
                "user@gmail.com",
                "recruiter@example.com",
                "Thanks",
                "Thanks for your message",
                Instant.now(),
                EmailCategory.REVISION_REQUEST,
                "revision",
                Instant.now(),
                true
        ));

        mockMvc.perform(post("/api/v1/email/messages/{id}/reply", messageId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReplyToEmailRequest(
                                List.of(documentId),
                                "Please find my resume attached."
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replied").value(true));

        verify(emailService).reply(eq(messageId), any());
    }

    private UpsertEmailAccountRequest accountRequest() {
        return new UpsertEmailAccountRequest(
                "user@gmail.com",
                "app-password",
                "imap.gmail.com",
                993,
                "smtp.gmail.com",
                587,
                true
        );
    }
}
