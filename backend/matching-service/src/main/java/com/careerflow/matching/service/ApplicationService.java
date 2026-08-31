package com.careerflow.matching.service;

import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.matching.client.JobClient;
import com.careerflow.matching.client.ProfileClient;
import com.careerflow.matching.dto.ApplicationResponse;
import com.careerflow.matching.dto.ApplicationStatus;
import com.careerflow.matching.dto.CreateApplicationRequest;
import com.careerflow.matching.dto.UpdateApplicationRequest;
import com.careerflow.matching.entity.JobApplication;
import com.careerflow.matching.repository.JobApplicationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ApplicationService {

    private final ProfileClient profileClient;
    private final JobClient jobClient;
    private final JobApplicationRepository repository;

    public ApplicationService(
            ProfileClient profileClient,
            JobClient jobClient,
            JobApplicationRepository repository
    ) {
        this.profileClient = profileClient;
        this.jobClient = jobClient;
        this.repository = repository;
    }

    @Transactional
    public ApplicationResponse create(CreateApplicationRequest request) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        assertResourcesAccessible(request.profileId(), request.jobId());

        if (repository.existsByOwnerIdAndProfileIdAndJobId(ownerId, request.profileId(), request.jobId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Application already tracked for this profile and job");
        }

        ApplicationStatus status = request.status() != null ? request.status() : ApplicationStatus.SAVED;
        JobApplication application = new JobApplication();
        application.setOwnerId(ownerId);
        application.setProfileId(request.profileId());
        application.setJobId(request.jobId());
        application.setStatus(status);
        application.setNotes(request.notes());
        if (status == ApplicationStatus.APPLIED) {
            application.setAppliedAt(Instant.now());
        }

        return toResponse(repository.save(application));
    }

    @Transactional
    public ApplicationResponse update(UUID id, UpdateApplicationRequest request) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        JobApplication application = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));

        if (request.status() != null) {
            application.setStatus(request.status());
            if (request.status() == ApplicationStatus.APPLIED && application.getAppliedAt() == null) {
                application.setAppliedAt(Instant.now());
            }
        }
        if (request.notes() != null) {
            application.setNotes(request.notes());
        }

        return toResponse(repository.save(application));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(UUID id) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        JobApplication application = repository.findByIdAndOwnerId(id, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        return toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll(ApplicationStatus status) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        List<JobApplication> applications = status == null
                ? repository.findByOwnerIdOrderByUpdatedAtDesc(ownerId)
                : repository.findByOwnerIdAndStatusOrderByUpdatedAtDesc(ownerId, status);
        return applications.stream().map(this::toResponse).toList();
    }

    private void assertResourcesAccessible(UUID profileId, UUID jobId) {
        try {
            profileClient.getProfile(profileId);
            jobClient.getJob(jobId);
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Profile or job not found or not accessible"
            );
        }
    }

    private ApplicationResponse toResponse(JobApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getProfileId(),
                application.getJobId(),
                application.getStatus(),
                application.getNotes(),
                application.getAppliedAt(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
