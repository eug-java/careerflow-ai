package com.careerflow.email.service;

import com.careerflow.email.entity.EmailAccountEntity;
import jakarta.mail.Session;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MailSessionFactoryTest {

    private final MailSessionFactory factory = new MailSessionFactory();

    @Test
    void createImapSessionShouldConfigureStoreProperties() {
        EmailAccountEntity account = account(true);

        Session session = factory.createImapSession(account, "password");

        assertThat(session.getProperty("mail.store.protocol")).isEqualTo("imaps");
        assertThat(session.getProperty("mail.imaps.host")).isEqualTo("imap.gmail.com");
        assertThat(session.getProperty("mail.imaps.port")).isEqualTo("993");
    }

    @Test
    void createSmtpSessionShouldConfigureTransportProperties() {
        EmailAccountEntity account = account(true);

        Session session = factory.createSmtpSession(account, "password");

        assertThat(session.getProperty("mail.transport.protocol")).isEqualTo("smtp");
        assertThat(session.getProperty("mail.smtp.host")).isEqualTo("smtp.gmail.com");
        assertThat(session.getProperty("mail.smtp.auth")).isEqualTo("true");
    }

    private EmailAccountEntity account(boolean useSsl) {
        return new EmailAccountEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "user@gmail.com",
                "imap.gmail.com",
                993,
                "smtp.gmail.com",
                587,
                useSsl,
                "encrypted"
        );
    }
}
