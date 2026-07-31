package com.careerflow.email.repository;

import com.careerflow.email.entity.EmailAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailAccountRepository extends JpaRepository<EmailAccountEntity, UUID> {

    Optional<EmailAccountEntity> findByOwnerId(UUID ownerId);
}
