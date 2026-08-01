package com.careerflow.aigeneration.repository;

import com.careerflow.aigeneration.entity.AiCredentialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AiCredentialRepository extends JpaRepository<AiCredentialEntity, UUID> {

    Optional<AiCredentialEntity> findByOwnerId(UUID ownerId);
}
