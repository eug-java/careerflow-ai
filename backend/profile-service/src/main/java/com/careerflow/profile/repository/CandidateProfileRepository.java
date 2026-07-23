package com.careerflow.profile.repository;

import com.careerflow.profile.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {

    List<CandidateProfile> findByOwnerId(UUID ownerId);

    Optional<CandidateProfile> findByIdAndOwnerId(UUID id, UUID ownerId);
}
