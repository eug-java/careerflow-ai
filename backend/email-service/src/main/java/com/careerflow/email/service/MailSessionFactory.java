package com.careerflow.email.service;

import com.careerflow.email.entity.EmailAccountEntity;
import jakarta.mail.*;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MailSessionFactory {

    public Session createImapSession(EmailAccountEntity account, String password) {
        Properties properties = baseProperties(account.isUseSsl());
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", account.getImapHost());
        properties.put("mail.imaps.port", String.valueOf(account.getImapPort()));
        return Session.getInstance(properties);
    }

    public Session createSmtpSession(EmailAccountEntity account, String password) {
        Properties properties = baseProperties(account.isUseSsl());
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", account.getSmtpHost());
        properties.put("mail.smtp.port", String.valueOf(account.getSmtpPort()));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", String.valueOf(account.isUseSsl()));
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getEmailAddress(), password);
            }
        });
    }

    public Store connectImapStore(Session session, EmailAccountEntity account, String password) throws MessagingException {
        Store store = session.getStore("imaps");
        store.connect(account.getImapHost(), account.getImapPort(), account.getEmailAddress(), password);
        return store;
    }

    private Properties baseProperties(boolean useSsl) {
        Properties properties = new Properties();
        properties.put("mail.imaps.ssl.enable", String.valueOf(useSsl));
        properties.put("mail.imaps.ssl.trust", "*");
        return properties;
    }
}
