# Email Integration

CareerFlow AI connects to a user's mailbox via IMAP/SMTP to track recruiter emails and send replies with generated documents.

## Features

- Configure mailbox credentials in the UI (`/email/settings`)
- Sync INBOX via IMAP
- Auto-classify messages:
  - **OFFER** — job offers
  - **REJECTION** — application rejections
  - **VACANCY** — new job postings from recruiters
  - **REVISION_REQUEST** — requests to send updated resume or cover letter
  - **OTHER** — everything else
- Reply to a message with PDF attachments from generated documents (resume / cover letter)
- Dashboard widget with category counts

## Architecture

```text
Frontend (/email)
      |
      v
API Gateway  /api/v1/email/**
      |
      v
email-service :8087
      |
      +--> PostgreSQL (careerflow_email :5438) — accounts, inbox cache
      +--> IMAP (read INBOX)
      +--> SMTP (send replies)
      +--> document-service (fetch PDF attachments)
```

## Security

- Mailbox password is encrypted at rest (AES-256-GCM)
- Encryption key: `CAREERFLOW_EMAIL_ENCRYPTION_KEY` (32 characters)
- Each user sees only their own account and messages (`ownerId` from JWT)
- Password is never returned in API responses

## Provider presets (UI)

| Provider | IMAP | SMTP |
|----------|------|------|
| Gmail | `imap.gmail.com:993` | `smtp.gmail.com:587` |
| Outlook | `outlook.office365.com:993` | `smtp.office365.com:587` |
| Yahoo | `imap.mail.yahoo.com:993` | `smtp.mail.yahoo.com:587` |

### Gmail setup

1. Enable IMAP in Gmail settings
2. Create an [App Password](https://myaccount.google.com/apppasswords)
3. Use the app password (not your main Gmail password) in CareerFlow settings

### Outlook setup

1. Enable IMAP in Outlook settings
2. Use an app password if MFA is enabled

## User flow

1. Open **Email → Settings**
2. Select provider preset (or enter IMAP/SMTP manually)
3. Enter email + app password
4. Click **Test connection**, then **Save account**
5. Open **Email → Sync inbox**
6. Filter by category (Offer, Rejection, Vacancy, etc.)
7. Select a message, choose generated documents, edit reply text
8. Click **Send reply with PDF attachments**

## API endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/email/account` | Current account (no password) |
| PUT | `/api/v1/email/account` | Save/update account |
| DELETE | `/api/v1/email/account` | Remove integration |
| POST | `/api/v1/email/account/test` | Test IMAP + SMTP |
| POST | `/api/v1/email/sync` | Fetch new messages |
| GET | `/api/v1/email/summary` | Counts by category |
| GET | `/api/v1/email/messages?category=` | List messages |
| GET | `/api/v1/email/messages/{id}` | Message detail |
| POST | `/api/v1/email/messages/{id}/reply` | Send reply with PDFs |

## Local development

```bash
# Infrastructure includes email-postgres on port 5438
make infra-up

# Run email-service (after document-service for reply attachments)
cd backend/email-service && mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8087/actuator/health
```

Swagger UI: http://localhost:8087/swagger-ui.html

## Troubleshooting

### Connection test fails (Gmail)

- Use an app password, not the regular account password
- Ensure IMAP is enabled in Gmail settings
- Check that "Less secure app access" / app passwords are allowed for your account type

### Sync returns 0 messages

- Verify INBOX has unread/recent messages
- Re-run sync — already imported UIDs are skipped (idempotent)

### Reply fails

- Ensure at least one document exists (generate resume/cover letter first)
- Verify document-service is running (PDF export)

### Invalid encryption key

Set a 32-character key in `.env`:

```bash
CAREERFLOW_EMAIL_ENCRYPTION_KEY=0123456789abcdef0123456789abcdef
```
