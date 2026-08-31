package com.careerflow.matching.repository;

import com.careerflow.matching.dto.ApplicationStatus;
import com.careerflow.matching.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Optional<JobApplication> findByIdAndOwnerId(UUID id, UUID ownerId);

    List<JobApplication> findByOwnerIdOrderByUpdatedAtDesc(UUID ownerId);

    List<JobApplication> findByOwnerIdAndStatusOrderByUpdatedAtDesc(UUID ownerId, ApplicationStatus status);

    boolean existsByOwnerIdAndProfileIdAndJobId(UUID ownerId, UUID profileId, UUID jobId);
}
