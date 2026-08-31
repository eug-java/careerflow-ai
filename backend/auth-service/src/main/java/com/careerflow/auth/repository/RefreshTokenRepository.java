package com.careerflow.auth.repository;

import com.careerflow.auth.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

    long deleteByExpiresAtBefore(Instant expiresAt);
}

