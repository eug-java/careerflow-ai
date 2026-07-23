package com.careerflow.matching.controller;

import com.careerflow.common.api.GlobalExceptionHandler;
import com.careerflow.common.security.CareerflowSecurityAutoConfiguration;
import com.careerflow.matching.dto.CreateMatchRequest;
import com.careerflow.matching.dto.MatchResultResponse;
import com.careerflow.matching.service.MatchingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MatchingController.class, excludeAutoConfiguration = CareerflowSecurityAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class MatchingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchingService service;

    @Test
    void calculateReturnsCreatedMatch() throws Exception {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        UUID matchId = UUID.randomUUID();
        MatchResultResponse response = response(matchId, profileId, jobId);
        when(service.calculate(any(CreateMatchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateMatchRequest(profileId, jobId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(matchId.toString()))
                .andExpect(jsonPath("$.profileId").value(profileId.toString()))
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.totalScore").value(91.25));

        verify(service).calculate(new CreateMatchRequest(profileId, jobId));
    }

    @Test
    void calculateReturnsBadRequestWhenProfileIdIsMissing() throws Exception {
        UUID jobId = UUID.randomUUID();
        String body = "{\"jobId\":\"" + jobId + "\"}";

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calculateReturnsBadRequestWhenJobIdIsMissing() throws Exception {
        UUID profileId = UUID.randomUUID();
        String body = "{\"profileId\":\"" + profileId + "\"}";

        mockMvc.perform(post("/api/v1/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByIdReturnsMatch() throws Exception {
        UUID id = UUID.randomUUID();
        MatchResultResponse response = response(id, UUID.randomUUID(), UUID.randomUUID());
        when(service.findById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/matches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.explanation").value("Strong match"));
    }

    @Test
    void findAllPassesOptionalFiltersToService() throws Exception {
        UUID profileId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        MatchResultResponse response = response(UUID.randomUUID(), profileId, jobId);
        when(service.findAll(profileId, jobId)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/matches")
                        .param("profileId", profileId.toString())
                        .param("jobId", jobId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].profileId").value(profileId.toString()))
                .andExpect(jsonPath("$[0].jobId").value(jobId.toString()));

        verify(service).findAll(profileId, jobId);
    }

    private static MatchResultResponse response(UUID id, UUID profileId, UUID jobId) {
        return new MatchResultResponse(
                id,
                profileId,
                jobId,
                new BigDecimal("91.25"),
                new BigDecimal("90.00"),
                new BigDecimal("100"),
                new BigDecimal("50"),
                "Strong match",
                Instant.parse("2026-05-23T12:00:00Z")
        );
    }
}
