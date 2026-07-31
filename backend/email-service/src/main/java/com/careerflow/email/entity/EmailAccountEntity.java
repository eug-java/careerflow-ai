package com.careerflow.email.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_accounts")
public class EmailAccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID ownerId;

    @Column(nullable = false)
    private String emailAddress;

    @Column(nullable = false)
    private String imapHost;

    @Column(nullable = false)
    private int imapPort;

    @Column(nullable = false)
    private String smtpHost;

    @Column(nullable = false)
    private int smtpPort;

    @Column(nullable = false)
    private boolean useSsl;

    @Column(nullable = false, columnDefinition = "text")
    private String encryptedPassword;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected EmailAccountEntity() {
    }

    public EmailAccountEntity(UUID id, UUID ownerId, String emailAddress, String imapHost, int imapPort,
                              String smtpHost, int smtpPort, boolean useSsl, String encryptedPassword) {
        this.id = id;
        this.ownerId = ownerId;
        this.emailAddress = emailAddress;
        this.imapHost = imapHost;
        this.imapPort = imapPort;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.useSsl = useSsl;
        this.encryptedPassword = encryptedPassword;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void update(String emailAddress, String imapHost, int imapPort, String smtpHost, int smtpPort,
                       boolean useSsl, String encryptedPassword) {
        this.emailAddress = emailAddress;
        this.imapHost = imapHost;
        this.imapPort = imapPort;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.useSsl = useSsl;
        this.encryptedPassword = encryptedPassword;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getImapHost() {
        return imapHost;
    }

    public int getImapPort() {
        return imapPort;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
