
package com.careerflow.profile.controller;

import com.careerflow.profile.dto.CandidateProfileResponse;
import com.careerflow.profile.dto.ExperienceResponse;
import com.careerflow.profile.dto.SkillResponse;
import com.careerflow.profile.exception.ProfileNotFoundException;
import com.careerflow.profile.service.CandidateProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CandidateProfileController.class)
class CandidateProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CandidateProfileService service;

    @Test
    void createShouldReturnCreatedProfile() throws Exception {
        CandidateProfileResponse response = response();
        when(service.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.fullName").value("Evgenii Buianov"))
                .andExpect(jsonPath("$.skills[0].name").value("Java"))
                .andExpect(jsonPath("$.experiences[0].companyName").value("Bank"));

        verify(service).create(any());
    }

    @Test
    void createShouldReturnBadRequestForInvalidPayload() throws Exception {
        String invalidJson = """
                {
                  "fullName": "",
                  "email": "not-an-email",
                  "skills": [
                    { "name": "" }
                  ],
                  "experiences": [
                    { "companyName": "", "positionTitle": "" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors", hasKey("fullName")))
                .andExpect(jsonPath("$.validationErrors", hasKey("email")));

        verifyNoInteractions(service);
    }

    @Test
    void findAllShouldReturnProfiles() throws Exception {
        when(service.findAll()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/profiles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Evgenii Buianov"))
                .andExpect(jsonPath("$[0].skills[0].category").value("Backend"));

        verify(service).findAll();
    }

    @Test
    void findByIdShouldReturnProfile() throws Exception {
        CandidateProfileResponse response = response();
        when(service.findById(response.id())).thenReturn(response);

        mockMvc.perform(get("/api/v1/profiles/{id}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.fullName").value("Evgenii Buianov"));

        verify(service).findById(response.id());
    }

    @Test
    void findByIdShouldReturnNotFoundWhenServiceThrows() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.findById(id)).thenThrow(new ProfileNotFoundException(id));

        mockMvc.perform(get("/api/v1/profiles/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Candidate profile not found: " + id))
                .andExpect(jsonPath("$.path").value("/api/v1/profiles/" + id));

        verify(service).findById(id);
    }

    @Test
    void updateShouldReturnUpdatedProfile() throws Exception {
        CandidateProfileResponse response = response();
        when(service.update(eq(response.id()), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/profiles/{id}", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.fullName").value("Evgenii Buianov"));

        verify(service).update(eq(response.id()), any());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/profiles/{id}", id))
                .andExpect(status().isNoContent());

        verify(service).delete(id);
    }

    private String validRequestJson() throws Exception {
        return objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("fullName", "Evgenii Buianov");
            put("professionalTitle", "Java Backend Developer");
            put("email", "eug.java.dev@gmail.com");
            put("phone", "+1 512 555 0100");
            put("location", "Austin, TX");
            put("summary", "Java backend engineer.");
            put("skills", List.of(new java.util.LinkedHashMap<>() {{
                put("name", "Java");
                put("category", "Backend");
                put("yearsOfExperience", new BigDecimal("6.0"));
            }}));
            put("experiences", List.of(new java.util.LinkedHashMap<>() {{
                put("companyName", "Bank");
                put("positionTitle", "Senior Java Developer");
                put("location", "Austin, TX");
                put("startDate", LocalDate.of(2021, 1, 1).toString());
                put("endDate", null);
                put("currentPosition", true);
                put("description", "Built microservices.");
            }}));
        }});
    }

    private static CandidateProfileResponse response() {
        UUID profileId = UUID.randomUUID();
        return new CandidateProfileResponse(
                profileId,
                "Evgenii Buianov",
                "Java Backend Developer",
                "eug.java.dev@gmail.com",
                "+1 512 555 0100",
                "Austin, TX",
                "Java backend engineer.",
                List.of(new SkillResponse(UUID.randomUUID(), "Java", "Backend", new BigDecimal("6.0"))),
                List.of(new ExperienceResponse(
                        UUID.randomUUID(),
                        "Bank",
                        "Senior Java Developer",
                        "Austin, TX",
                        LocalDate.of(2021, 1, 1),
                        null,
                        true,
                        "Built microservices."
                )),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-02T00:00:00Z")
        );
    }
}
