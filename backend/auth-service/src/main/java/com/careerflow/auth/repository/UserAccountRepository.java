package com.careerflow.auth.repository;

import com.careerflow.auth.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccountEntity, UUID> {

    Optional<UserAccountEntity> findByUsername(String username);

    boolean existsByUsername(String username);
}
