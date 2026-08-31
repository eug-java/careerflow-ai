package com.careerflow.matching.controller;

import com.careerflow.common.api.GlobalExceptionHandler;
import com.careerflow.common.security.CareerflowSecurityAutoConfiguration;
import com.careerflow.matching.dto.ApplicationResponse;
import com.careerflow.matching.dto.ApplicationStatus;
import com.careerflow.matching.dto.CreateApplicationRequest;
import com.careerflow.matching.dto.UpdateApplicationRequest;
import com.careerflow.matching.service.ApplicationService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationController.class, excludeAutoConfiguration = CareerflowSecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationService service;

    @Test
    void createReturnsCreatedApplication() throws Exception {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        ApplicationResponse response = response(UUID.randomUUID(), profileId, jobId, ApplicationStatus.SAVED);
        when(service.create(any(CreateApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateApplicationRequest(profileId, jobId, null, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").value(profileId.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("SAVED"));
    }

    @Test
    void updateReturnsUpdatedApplication() throws Exception {
        UUID id = UUID.randomUUID();
        ApplicationResponse response = response(id, UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.APPLIED);
        when(service.update(eq(id), any(UpdateApplicationRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/applications/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPLIED"));

        verify(service).update(eq(id), any(UpdateApplicationRequest.class));
    }

    @Test
    void findAllPassesStatusFilter() throws Exception {
        ApplicationResponse response = response(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), ApplicationStatus.INTERVIEW);
        when(service.findAll(ApplicationStatus.INTERVIEW)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/applications").param("status", "INTERVIEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("INTERVIEW"));

        verify(service).findAll(ApplicationStatus.INTERVIEW);
    }

    private static ApplicationResponse response(UUID id, UUID profileId, UUID jobId, ApplicationStatus status) {
        Instant now = Instant.parse("2026-05-23T12:00:00Z");
        return new ApplicationResponse(id, profileId, jobId, status, null, null, now, now);
    }
}
