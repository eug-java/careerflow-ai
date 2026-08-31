package com.careerflow.email.service;

import com.careerflow.common.api.ForbiddenException;
import com.careerflow.common.api.ResourceNotFoundException;
import com.careerflow.common.test.TestAuthSupport;
import com.careerflow.email.client.DocumentClient;
import com.careerflow.email.dto.*;
import com.careerflow.email.entity.EmailAccountEntity;
import com.careerflow.email.entity.InboxMessageEntity;
import com.careerflow.email.repository.EmailAccountRepository;
import com.careerflow.email.repository.InboxMessageRepository;
import com.careerflow.common.security.CredentialEncryptor;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String ENCRYPTION_KEY = "0123456789abcdef0123456789abcdef";

    @Mock
    private EmailAccountRepository accountRepository;

    @Mock
    private InboxMessageRepository messageRepository;

    @Mock
    private MailSessionFactory mailSessionFactory;

    @Mock
    private EmailClassifier emailClassifier;

    @Mock
    private DocumentClient documentClient;

    private EmailService emailService;

    private CredentialEncryptor credentialEncryptor;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        credentialEncryptor = new CredentialEncryptor(ENCRYPTION_KEY);
        emailService = new EmailService(
                accountRepository,
                messageRepository,
                credentialEncryptor,
                mailSessionFactory,
                emailClassifier,
                documentClient
        );
        ownerId = TestAuthSupport.authenticateTestUser();
    }

    @AfterEach
    void tearDown() {
        TestAuthSupport.clear();
    }

    @Test
    void getAccountReturnsEmptyWhenMissing() {
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        EmailAccountResponse response = emailService.getAccount();

        assertThat(response.configured()).isFalse();
        assertThat(response.emailAddress()).isNull();
    }

    @Test
    void getAccountReturnsExistingAccount() {
        EmailAccountEntity account = sampleAccount();
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(account));

        EmailAccountResponse response = emailService.getAccount();

        assertThat(response.configured()).isTrue();
        assertThat(response.emailAddress()).isEqualTo("user@gmail.com");
        assertThat(response.imapHost()).isEqualTo("imap.gmail.com");
    }

    @Test
    void upsertAccountCreatesNewAccount() {
        UpsertEmailAccountRequest request = accountRequest();
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());
        when(accountRepository.save(any(EmailAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EmailAccountResponse response = emailService.upsertAccount(request);

        ArgumentCaptor<EmailAccountEntity> captor = ArgumentCaptor.forClass(EmailAccountEntity.class);
        verify(accountRepository).save(captor.capture());
        EmailAccountEntity saved = captor.getValue();

        assertThat(saved.getOwnerId()).isEqualTo(ownerId);
        assertThat(saved.getEmailAddress()).isEqualTo("user@gmail.com");
        assertThat(credentialEncryptor.decrypt(saved.getEncryptedPassword())).isEqualTo("app-password");
        assertThat(response.configured()).isTrue();
    }

    @Test
    void upsertAccountUpdatesExistingAccount() {
        EmailAccountEntity existing = sampleAccount();
        UpsertEmailAccountRequest request = new UpsertEmailAccountRequest(
                "updated@gmail.com",
                "new-password",
                "imap.gmail.com",
                993,
                "smtp.gmail.com",
                587,
                true
        );
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(existing));
        when(accountRepository.save(existing)).thenReturn(existing);

        EmailAccountResponse response = emailService.upsertAccount(request);

        assertThat(existing.getEmailAddress()).isEqualTo("updated@gmail.com");
        assertThat(credentialEncryptor.decrypt(existing.getEncryptedPassword())).isEqualTo("new-password");
        assertThat(response.emailAddress()).isEqualTo("updated@gmail.com");
    }

    @Test
    void deleteAccountRemovesExistingAccount() {
        EmailAccountEntity account = sampleAccount();
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(account));

        emailService.deleteAccount();

        verify(accountRepository).delete(account);
    }

    @Test
    void listMessagesReturnsAllMessagesWhenCategoryMissing() {
        InboxMessageEntity message = sampleMessage(EmailCategory.OFFER);
        when(messageRepository.findByOwnerIdOrderByReceivedAtDesc(ownerId)).thenReturn(List.of(message));

        List<InboxMessageResponse> responses = emailService.listMessages(null);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().category()).isEqualTo(EmailCategory.OFFER);
    }

    @Test
    void listMessagesFiltersByCategory() {
        InboxMessageEntity message = sampleMessage(EmailCategory.REJECTION);
        when(messageRepository.findByOwnerIdAndCategoryOrderByReceivedAtDesc(ownerId, EmailCategory.REJECTION))
                .thenReturn(List.of(message));

        List<InboxMessageResponse> responses = emailService.listMessages(EmailCategory.REJECTION);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().category()).isEqualTo(EmailCategory.REJECTION);
    }

    @Test
    void getMessageReturnsOwnedMessage() {
        InboxMessageEntity message = sampleMessage(EmailCategory.VACANCY);
        when(messageRepository.findById(message.getId())).thenReturn(Optional.of(message));

        InboxMessageResponse response = emailService.getMessage(message.getId());

        assertThat(response.id()).isEqualTo(message.getId());
        assertThat(response.subject()).isEqualTo("Senior Java role");
    }

    @Test
    void getMessageFailsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(messageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailService.getMessage(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getMessageFailsWhenOwnedByAnotherUser() {
        UUID messageId = UUID.randomUUID();
        InboxMessageEntity foreignMessage = new InboxMessageEntity(
                messageId,
                UUID.randomUUID(),
                1L,
                null,
                "INBOX",
                "Hello",
                "recruiter@example.com",
                "user@gmail.com",
                "Hello",
                "Hello",
                Instant.now(),
                EmailCategory.OTHER,
                "fallback"
        );
        when(messageRepository.findById(messageId)).thenReturn(Optional.of(foreignMessage));

        assertThatThrownBy(() -> emailService.getMessage(messageId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void summaryAggregatesCountsAndAccountPresence() {
        when(messageRepository.countByOwnerId(ownerId)).thenReturn(4L);
        when(messageRepository.countByOwnerIdAndCategory(ownerId, EmailCategory.OFFER)).thenReturn(1L);
        when(messageRepository.countByOwnerIdAndCategory(ownerId, EmailCategory.REJECTION)).thenReturn(1L);
        when(messageRepository.countByOwnerIdAndCategory(ownerId, EmailCategory.VACANCY)).thenReturn(1L);
        when(messageRepository.countByOwnerIdAndCategory(ownerId, EmailCategory.REVISION_REQUEST)).thenReturn(1L);
        when(messageRepository.countByOwnerIdAndCategory(ownerId, EmailCategory.OTHER)).thenReturn(0L);
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(sampleAccount()));

        EmailSummaryResponse summary = emailService.summary();

        assertThat(summary.totalMessages()).isEqualTo(4);
        assertThat(summary.byCategory().get(EmailCategory.OFFER)).isEqualTo(1L);
        assertThat(summary.accountConfigured()).isTrue();
    }

    @Test
    void testConnectionReturnsSuccessWhenMailChecksPass() throws Exception {
        UpsertEmailAccountRequest request = accountRequest();
        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        Session smtpSession = mock(Session.class);
        Transport transport = mock(Transport.class);

        when(mailSessionFactory.createImapSession(any(), eq("app-password"))).thenReturn(mock(Session.class));
        when(mailSessionFactory.connectImapStore(any(), any(), eq("app-password"))).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(mailSessionFactory.createSmtpSession(any(), eq("app-password"))).thenReturn(smtpSession);
        when(smtpSession.getTransport("smtp")).thenReturn(transport);

        ConnectionTestResponse response = emailService.testConnection(request);

        assertThat(response.success()).isTrue();
        verify(folder).open(Folder.READ_ONLY);
        verify(folder).close(false);
        verify(transport).connect("smtp.gmail.com", 587, "user@gmail.com", "app-password");
        verify(transport).close();
    }

    @Test
    void testConnectionReturnsFailureWhenImapFails() throws Exception {
        UpsertEmailAccountRequest request = accountRequest();
        when(mailSessionFactory.createImapSession(any(), any())).thenReturn(mock(Session.class));
        when(mailSessionFactory.connectImapStore(any(), any(), any()))
                .thenThrow(new MessagingException("Authentication failed"));

        ConnectionTestResponse response = emailService.testConnection(request);

        assertThat(response.success()).isFalse();
        assertThat(response.message()).contains("Authentication failed");
    }

    @Test
    void syncInboxImportsNewMessages() throws Exception {
        EmailAccountEntity account = sampleAccount();
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.of(account));
        when(messageRepository.findByOwnerIdAndFolderAndMessageUid(eq(ownerId), eq("INBOX"), anyLong()))
                .thenReturn(Optional.empty());
        when(emailClassifier.classify(anyString(), anyString()))
                .thenReturn(new EmailClassifier.ClassificationResult(EmailCategory.OFFER, "offer keywords"));
        when(messageRepository.countByOwnerId(ownerId)).thenReturn(1L);

        Store store = mock(Store.class);
        Folder folder = mock(Folder.class);
        MimeMessage mimeMessage = buildMimeMessage("Job offer", "We are pleased to offer you the role");

        when(mailSessionFactory.createImapSession(any(), any())).thenReturn(Session.getInstance(new Properties()));
        when(mailSessionFactory.connectImapStore(any(), any(), any())).thenReturn(store);
        when(store.getFolder("INBOX")).thenReturn(folder);
        when(folder.getMessages()).thenReturn(new Message[]{mimeMessage});

        SyncEmailResponse response = emailService.syncInbox();

        assertThat(response.importedCount()).isEqualTo(1);
        assertThat(response.totalInboxCount()).isEqualTo(1);
        verify(messageRepository).save(any(InboxMessageEntity.class));
        verify(folder).close(false);
    }

    @Test
    void syncInboxFailsWhenAccountMissing() {
        when(accountRepository.findByOwnerId(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailService.syncInbox())
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private UpsertEmailAccountRequest accountRequest() {
        return new UpsertEmailAccountRequest(
                "user@gmail.com",
                "app-password",
                "imap.gmail.com",
                993,
                "smtp.gmail.com",
                587,
                true
        );
    }

    private EmailAccountEntity sampleAccount() {
        return new EmailAccountEntity(
                UUID.randomUUID(),
                ownerId,
                "user@gmail.com",
                "imap.gmail.com",
                993,
                "smtp.gmail.com",
                587,
                true,
                credentialEncryptor.encrypt("app-password")
        );
    }

    private InboxMessageEntity sampleMessage(EmailCategory category) {
        return new InboxMessageEntity(
                UUID.randomUUID(),
                ownerId,
                42L,
                "<msg-1@example.com>",
                "INBOX",
                "Senior Java role",
                "recruiter@example.com",
                "user@gmail.com",
                "We are hiring",
                "We are hiring a senior Java engineer",
                Instant.parse("2026-07-01T10:00:00Z"),
                category,
                "vacancy keywords"
        );
    }

    private MimeMessage buildMimeMessage(String subject, String body) throws Exception {
        Session session = Session.getInstance(new Properties());
        MimeMessage message = new MimeMessage(session);
        message.setSubject(subject);
        message.setFrom(new InternetAddress("recruiter@example.com"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("user@gmail.com"));
        message.setText(body);
        message.setHeader("Message-ID", "<offer-1@example.com>");
        return message;
    }
}
