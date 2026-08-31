package com.careerflow.email.service;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.security.CurrentUserProvider;
import com.careerflow.email.client.DocumentClient;
import com.careerflow.email.dto.*;
import com.careerflow.email.entity.EmailAccountEntity;
import com.careerflow.email.entity.InboxMessageEntity;
import com.careerflow.email.repository.EmailAccountRepository;
import com.careerflow.email.repository.InboxMessageRepository;
import com.careerflow.common.security.CredentialEncryptor;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.UIDFolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Service
public class EmailService {

    private static final String INBOX_FOLDER = "INBOX";
    private static final int PREVIEW_LENGTH = 500;

    private final EmailAccountRepository accountRepository;
    private final InboxMessageRepository messageRepository;
    private final CredentialEncryptor credentialEncryptor;
    private final MailSessionFactory mailSessionFactory;
    private final EmailClassifier emailClassifier;
    private final DocumentClient documentClient;

    public EmailService(
            EmailAccountRepository accountRepository,
            InboxMessageRepository messageRepository,
            CredentialEncryptor credentialEncryptor,
            MailSessionFactory mailSessionFactory,
            EmailClassifier emailClassifier,
            DocumentClient documentClient
    ) {
        this.accountRepository = accountRepository;
        this.messageRepository = messageRepository;
        this.credentialEncryptor = credentialEncryptor;
        this.mailSessionFactory = mailSessionFactory;
        this.emailClassifier = emailClassifier;
        this.documentClient = documentClient;
    }

    @Transactional(readOnly = true)
    public EmailAccountResponse getAccount() {
        UUID ownerId = CurrentUserProvider.requireUserId();
        return accountRepository.findByOwnerId(ownerId)
                .map(this::toAccountResponse)
                .orElse(new EmailAccountResponse(null, null, 0, null, 0, true, null, false));
    }

    @Transactional
    public EmailAccountResponse upsertAccount(UpsertEmailAccountRequest request) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        String encryptedPassword = credentialEncryptor.encrypt(request.password());
        EmailAccountEntity entity = accountRepository.findByOwnerId(ownerId)
                .map(existing -> {
                    existing.update(
                            request.emailAddress(),
                            request.imapHost(),
                            request.imapPort(),
                            request.smtpHost(),
                            request.smtpPort(),
                            request.useSsl(),
                            encryptedPassword
                    );
                    return existing;
                })
                .orElseGet(() -> new EmailAccountEntity(
                        UUID.randomUUID(),
                        ownerId,
                        request.emailAddress(),
                        request.imapHost(),
                        request.imapPort(),
                        request.smtpHost(),
                        request.smtpPort(),
                        request.useSsl(),
                        encryptedPassword
                ));
        return toAccountResponse(accountRepository.save(entity));
    }

    @Transactional
    public void deleteAccount() {
        UUID ownerId = CurrentUserProvider.requireUserId();
        accountRepository.findByOwnerId(ownerId).ifPresent(accountRepository::delete);
    }

    public ConnectionTestResponse testConnection(UpsertEmailAccountRequest request) {
        EmailAccountEntity temp = new EmailAccountEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                request.emailAddress(),
                request.imapHost(),
                request.imapPort(),
                request.smtpHost(),
                request.smtpPort(),
                request.useSsl(),
                credentialEncryptor.encrypt(request.password())
        );
        try {
            testImap(temp, request.password());
            testSmtp(temp, request.password());
            return new ConnectionTestResponse(true, "IMAP and SMTP connections succeeded");
        } catch (MessagingException ex) {
            return new ConnectionTestResponse(false, ex.getMessage());
        }
    }

    @Transactional
    public SyncEmailResponse syncInbox() {
        EmailAccountEntity account = requireAccount();
        String password = credentialEncryptor.decrypt(account.getEncryptedPassword());
        int imported = 0;

        try {
            Session session = mailSessionFactory.createImapSession(account, password);
            try (Store store = mailSessionFactory.connectImapStore(session, account, password)) {
                Folder inbox = store.getFolder(INBOX_FOLDER);
                inbox.open(Folder.READ_ONLY);
                UIDFolder uidFolder = inbox instanceof UIDFolder folder ? folder : null;
                Message[] messages = inbox.getMessages();
                UUID ownerId = account.getOwnerId();

                for (Message message : messages) {
                    long uid = uidFolder != null ? uidFolder.getUID(message) : message.getMessageNumber();
                    if (messageRepository.findByOwnerIdAndFolderAndMessageUid(ownerId, INBOX_FOLDER, uid).isPresent()) {
                        continue;
                    }
                    String subject = message.getSubject();
                    String body = extractText(message);
                    EmailClassifier.ClassificationResult classification = emailClassifier.classify(subject, body);
                    InboxMessageEntity entity = new InboxMessageEntity(
                            UUID.randomUUID(),
                            ownerId,
                            uid,
                            extractHeader(message, "Message-ID"),
                            INBOX_FOLDER,
                            subject,
                            extractAddress(message.getFrom()),
                            extractAddress(message.getRecipients(Message.RecipientType.TO)),
                            preview(body),
                            body,
                            Instant.ofEpochMilli(
                                    message.getReceivedDate() != null
                                            ? message.getReceivedDate().getTime()
                                            : System.currentTimeMillis()
                            ),
                            classification.category(),
                            classification.reason()
                    );
                    messageRepository.save(entity);
                    imported++;
                }
                inbox.close(false);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sync inbox: " + ex.getMessage(), ex);
        }

        long total = messageRepository.countByOwnerId(account.getOwnerId());
        return new SyncEmailResponse(imported, (int) total);
    }

    @Transactional(readOnly = true)
    public List<InboxMessageResponse> listMessages(EmailCategory category) {
        UUID ownerId = CurrentUserProvider.requireUserId();
        List<InboxMessageEntity> messages = category == null
                ? messageRepository.findByOwnerIdOrderByReceivedAtDesc(ownerId)
                : messageRepository.findByOwnerIdAndCategoryOrderByReceivedAtDesc(ownerId, category);
        return messages.stream().map(this::toMessageResponse).toList();
    }

    @Transactional(readOnly = true)
    public InboxMessageResponse getMessage(UUID id) {
        return toMessageResponse(requireOwnedMessage(id));
    }

    @Transactional(readOnly = true)
    public EmailSummaryResponse summary() {
        UUID ownerId = CurrentUserProvider.requireUserId();
        Map<EmailCategory, Long> byCategory = new EnumMap<>(EmailCategory.class);
        for (EmailCategory category : EmailCategory.values()) {
            byCategory.put(category, messageRepository.countByOwnerIdAndCategory(ownerId, category));
        }
        return new EmailSummaryResponse(
                messageRepository.countByOwnerId(ownerId),
                byCategory,
                accountRepository.findByOwnerId(ownerId).isPresent()
        );
    }

    @Transactional
    public InboxMessageResponse reply(UUID messageId, ReplyToEmailRequest request) {
        InboxMessageEntity inboxMessage = requireOwnedMessage(messageId);
        EmailAccountEntity account = requireAccount();
        String password = credentialEncryptor.decrypt(account.getEncryptedPassword());

        try {
            Session session = mailSessionFactory.createSmtpSession(account, password);
            MimeMessage reply = new MimeMessage(session);
            reply.setFrom(new InternetAddress(account.getEmailAddress()));
            reply.setRecipients(Message.RecipientType.TO, InternetAddress.parse(inboxMessage.getFromAddress()));
            reply.setSubject(buildReplySubject(inboxMessage.getSubject()));
            if (inboxMessage.getInternetMessageId() != null) {
                reply.setHeader("In-Reply-To", inboxMessage.getInternetMessageId());
                reply.setHeader("References", inboxMessage.getInternetMessageId());
            }

            MimeMultipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(request.bodyText(), "utf-8");
            multipart.addBodyPart(textPart);

            for (UUID documentId : request.documentIds()) {
                byte[] pdf = documentClient.downloadPdf(documentId);
                String fileName = documentClient.fetchFileName(documentId);
                if (!fileName.endsWith(".pdf")) {
                    fileName = fileName.replace(".md", ".pdf");
                }
                MimeBodyPart attachment = new MimeBodyPart();
                attachment.setFileName(fileName);
                attachment.setContent(pdf, "application/pdf");
                multipart.addBodyPart(attachment);
            }

            reply.setContent(multipart);
            Transport.send(reply);
            inboxMessage.markReplied();
            return toMessageResponse(inboxMessage);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to send reply: " + ex.getMessage(), ex);
        }
    }

    private EmailAccountEntity requireAccount() {
        UUID ownerId = CurrentUserProvider.requireUserId();
        return accountRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Email account is not configured"));
    }

    private InboxMessageEntity requireOwnedMessage(UUID id) {
        InboxMessageEntity message = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email message not found: " + id));
        UUID ownerId = CurrentUserProvider.requireUserId();
        if (!ownerId.equals(message.getOwnerId())) {
            throw new ForbiddenException("Email message access denied");
        }
        return message;
    }

    private EmailAccountResponse toAccountResponse(EmailAccountEntity entity) {
        return new EmailAccountResponse(
                entity.getEmailAddress(),
                entity.getImapHost(),
                entity.getImapPort(),
                entity.getSmtpHost(),
                entity.getSmtpPort(),
                entity.isUseSsl(),
                entity.getUpdatedAt(),
                true
        );
    }

    private InboxMessageResponse toMessageResponse(InboxMessageEntity entity) {
        return new InboxMessageResponse(
                entity.getId(),
                entity.getSubject(),
                entity.getFromAddress(),
                entity.getToAddress(),
                entity.getBodyPreview(),
                entity.getBodyText(),
                entity.getReceivedAt(),
                entity.getCategory(),
                entity.getClassificationReason(),
                entity.getRepliedAt(),
                entity.getRepliedAt() != null
        );
    }

    private void testImap(EmailAccountEntity account, String password) throws MessagingException {
        Session session = mailSessionFactory.createImapSession(account, password);
        try (Store store = mailSessionFactory.connectImapStore(session, account, password)) {
            Folder folder = store.getFolder(INBOX_FOLDER);
            folder.open(Folder.READ_ONLY);
            folder.close(false);
        }
    }

    private void testSmtp(EmailAccountEntity account, String password) throws MessagingException {
        Session session = mailSessionFactory.createSmtpSession(account, password);
        Transport transport = session.getTransport("smtp");
        transport.connect(account.getSmtpHost(), account.getSmtpPort(), account.getEmailAddress(), password);
        transport.close();
    }

    private String buildReplySubject(String subject) {
        if (subject == null || subject.isBlank()) {
            return "Re: Application materials";
        }
        if (subject.toLowerCase(Locale.ROOT).startsWith("re:")) {
            return subject;
        }
        return "Re: " + subject;
    }

    private String extractText(Part part) throws MessagingException, IOException {
        if (part.isMimeType("text/plain")) {
            return Objects.toString(part.getContent(), "");
        }
        if (part.isMimeType("text/html")) {
            return Objects.toString(part.getContent(), "").replaceAll("<[^>]+>", " ");
        }
        if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                builder.append(extractText(multipart.getBodyPart(i))).append('\n');
            }
            return builder.toString();
        }
        return "";
    }

    private String extractAddress(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        return addresses[0].toString();
    }

    private String extractHeader(Message message, String header) throws MessagingException {
        String[] values = message.getHeader(header);
        if (values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }

    private String preview(String body) {
        if (body == null) {
            return "";
        }
        String normalized = body.replaceAll("\\s+", " ").trim();
        return normalized.length() <= PREVIEW_LENGTH
                ? normalized
                : normalized.substring(0, PREVIEW_LENGTH) + "...";
    }
}
