package com.careerflow.email.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailAccountEntityTest {

    @Test
    void updateShouldRefreshFieldsAndTimestamp() {
        EmailAccountEntity entity = new EmailAccountEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "old@gmail.com",
                "imap.gmail.com",
                993,
                "smtp.gmail.com",
                587,
                true,
                "encrypted"
        );

        entity.update(
                "new@gmail.com",
                "outlook.office365.com",
                993,
                "smtp.office365.com",
                587,
                false,
                "new-encrypted"
        );

        assertThat(entity.getEmailAddress()).isEqualTo("new@gmail.com");
        assertThat(entity.getImapHost()).isEqualTo("outlook.office365.com");
        assertThat(entity.isUseSsl()).isFalse();
        assertThat(entity.getEncryptedPassword()).isEqualTo("new-encrypted");
        assertThat(entity.getUpdatedAt()).isNotNull();
    }
}
