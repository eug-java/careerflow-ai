package com.careerflow.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "oauth_identities")
public class OAuthIdentityEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "provider_subject", nullable = false, length = 128)
    private String providerSubject;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OAuthIdentityEntity() {
    }

    public OAuthIdentityEntity(String provider, String providerSubject, UUID userId) {
        this.provider = provider;
        this.providerSubject = providerSubject;
        this.userId = userId;
    }

    @PrePersist
    void prePersist() {
        this.id = UUID.randomUUID();
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getProviderSubject() {
        return providerSubject;
    }
}
