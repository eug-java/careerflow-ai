package com.careerflow.matching.service;

import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.matching.client.JobClient;
import com.careerflow.matching.client.JobResponse;
import com.careerflow.matching.client.ProfileClient;
import com.careerflow.matching.client.ProfileResponse;
import com.careerflow.matching.dto.ApplicationStatus;
import com.careerflow.matching.dto.CreateApplicationRequest;
import com.careerflow.matching.dto.UpdateApplicationRequest;
import com.careerflow.matching.entity.JobApplication;
import com.careerflow.matching.repository.JobApplicationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ProfileClient profileClient;

    @Mock
    private JobClient jobClient;

    @Mock
    private JobApplicationRepository repository;

    private ApplicationService service;
    private UUID ownerId;
    private UUID profileId;
    private UUID jobId;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(profileClient, jobClient, repository);
        ownerId = TestAuthSupport.authenticateTestUser();
        profileId = UUID.randomUUID();
        jobId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void createPersistsSavedApplication() {
        when(profileClient.getProfile(profileId)).thenReturn(sampleProfile(profileId));
        when(jobClient.getJob(jobId)).thenReturn(sampleJob(jobId));
        when(repository.existsByOwnerIdAndProfileIdAndJobId(ownerId, profileId, jobId)).thenReturn(false);
        when(repository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication application = invocation.getArgument(0);
            application.getClass().getDeclaredFields();
            return application;
        });

        var response = service.create(new CreateApplicationRequest(profileId, jobId, null, "Interested"));

        assertThat(response.profileId()).isEqualTo(profileId);
        assertThat(response.jobId()).isEqualTo(jobId);
        assertThat(response.status()).isEqualTo(ApplicationStatus.SAVED);
        assertThat(response.notes()).isEqualTo("Interested");

        ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOwnerId()).isEqualTo(ownerId);
    }

    @Test
    void createRejectsDuplicateApplication() {
        when(profileClient.getProfile(profileId)).thenReturn(sampleProfile(profileId));
        when(jobClient.getJob(jobId)).thenReturn(sampleJob(jobId));
        when(repository.existsByOwnerIdAndProfileIdAndJobId(ownerId, profileId, jobId)).thenReturn(true);

        assertThatThrownBy(() -> service.create(new CreateApplicationRequest(profileId, jobId, null, null)))
                .isInstanceOf(ResponseStatusException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void updateSetsAppliedAtWhenStatusChangesToApplied() {
        UUID applicationId = UUID.randomUUID();
        JobApplication application = new JobApplication();
        application.setOwnerId(ownerId);
        application.setProfileId(profileId);
        application.setJobId(jobId);
        application.setStatus(ApplicationStatus.SAVED);
        when(repository.findByIdAndOwnerId(applicationId, ownerId)).thenReturn(Optional.of(application));
        when(repository.save(application)).thenReturn(application);

        var response = service.update(applicationId, new UpdateApplicationRequest(ApplicationStatus.APPLIED, null));

        assertThat(response.status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.appliedAt()).isNotNull();
    }

    @Test
    void findAllFiltersByStatus() {
        JobApplication saved = applicationWithStatus(ApplicationStatus.SAVED);
        when(repository.findByOwnerIdAndStatusOrderByUpdatedAtDesc(ownerId, ApplicationStatus.SAVED))
                .thenReturn(List.of(saved));

        var responses = service.findAll(ApplicationStatus.SAVED);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().status()).isEqualTo(ApplicationStatus.SAVED);
    }

    @Test
    void createRejectsInaccessibleResources() {
        WebClientResponseException notFound = WebClientResponseException.create(
                404,
                "Not Found",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8
        );
        when(profileClient.getProfile(profileId)).thenThrow(notFound);

        assertThatThrownBy(() -> service.create(new CreateApplicationRequest(profileId, jobId, null, null)))
                .isInstanceOf(ResponseStatusException.class);
    }

    private JobApplication applicationWithStatus(ApplicationStatus status) {
        JobApplication application = new JobApplication();
        application.setOwnerId(ownerId);
        application.setProfileId(profileId);
        application.setJobId(jobId);
        application.setStatus(status);
        return application;
    }

    private ProfileResponse sampleProfile(UUID id) {
        return new ProfileResponse(
                id,
                "Demo User",
                "Engineer",
                "demo@example.com",
                null,
                "Remote",
                null,
                null,
                List.of(),
                List.of()
        );
    }

    private JobResponse sampleJob(UUID id) {
        return new JobResponse(
                id,
                "Backend Engineer",
                "Acme",
                "Remote",
                "Full-time",
                null,
                null,
                null,
                true,
                "Build APIs",
                List.of()
        );
    }
}
