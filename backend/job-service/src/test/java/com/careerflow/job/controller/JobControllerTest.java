package com.careerflow.job.controller;

import com.careerflow.job.dto.JobResponse;
import com.careerflow.job.dto.JobSkillResponse;
import com.careerflow.job.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JobService service;

    @Test
    void createShouldReturnCreatedJobResponse() throws Exception {
        JobResponse response = response(UUID.randomUUID(), "Java Developer");
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestJson())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.skills", hasSize(1)))
                .andExpect(jsonPath("$.skills[0].name").value("Java"));

        verify(service).create(any());
    }

    @Test
    void findAllShouldReturnJobList() throws Exception {
        when(service.findAll()).thenReturn(List.of(
                response(UUID.randomUUID(), "Java Developer"),
                response(UUID.randomUUID(), "QA Engineer")
        ));

        mockMvc.perform(get("/api/v1/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[1].title").value("QA Engineer"));
    }

    @Test
    void findByIdShouldReturnJob() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenReturn(response(id, "Java Developer"));

        mockMvc.perform(get("/api/v1/jobs/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void updateShouldReturnUpdatedJob() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(eq(id), any())).thenReturn(response(id, "Updated Java Developer"));

        mockMvc.perform(put("/api/v1/jobs/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestJson())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Updated Java Developer"));

        verify(service).update(eq(id), any());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/v1/jobs/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }

    private static JobResponse response(UUID id, String title) {
        return new JobResponse(
                id,
                title,
                "CareerFlow",
                "Austin, TX",
                "FULL_TIME",
                new BigDecimal("100000.00"),
                new BigDecimal("150000.00"),
                "USD",
                true,
                "Build backend services",
                List.of(new JobSkillResponse(UUID.randomUUID(), "Java", true)),
                Instant.parse("2026-05-23T10:15:30Z")
        );
    }

    private static java.util.Map<String, Object> requestJson() {
        return java.util.Map.of(
                "title", "Java Developer",
                "companyName", "CareerFlow",
                "location", "Austin, TX",
                "employmentType", "FULL_TIME",
                "salaryMin", "100000.00",
                "salaryMax", "150000.00",
                "currency", "USD",
                "remote", true,
                "description", "Build backend services",
                "skills", List.of(java.util.Map.of("name", "Java", "required", true))
        );
    }
}
