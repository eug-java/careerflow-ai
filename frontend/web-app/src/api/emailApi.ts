import { apiClient } from "./client";

export type EmailCategory = "OFFER" | "REJECTION" | "VACANCY" | "REVISION_REQUEST" | "OTHER";

export interface EmailAccount {
    emailAddress: string | null;
    imapHost: string | null;
    imapPort: number;
    smtpHost: string | null;
    smtpPort: number;
    useSsl: boolean;
    updatedAt: string | null;
    configured: boolean;
}

export interface UpsertEmailAccountRequest {
    emailAddress: string;
    password: string;
    imapHost: string;
    imapPort: number;
    smtpHost: string;
    smtpPort: number;
    useSsl: boolean;
}

export interface InboxMessage {
    id: string;
    subject: string;
    fromAddress: string;
    toAddress: string;
    bodyPreview: string;
    bodyText: string;
    receivedAt: string;
    category: EmailCategory;
    classificationReason: string;
    repliedAt: string | null;
    replied: boolean;
}

export interface EmailSummary {
    totalMessages: number;
    byCategory: Record<EmailCategory, number>;
    accountConfigured: boolean;
}

export interface SyncEmailResponse {
    importedCount: number;
    totalInboxCount: number;
}

export interface ConnectionTestResponse {
    success: boolean;
    message: string;
}

export interface ReplyToEmailRequest {
    documentIds: string[];
    bodyText: string;
}

export async function fetchEmailAccount(): Promise<EmailAccount> {
    const response = await apiClient.get<EmailAccount>("/api/v1/email/account");
    return response.data;
}

export async function upsertEmailAccount(request: UpsertEmailAccountRequest): Promise<EmailAccount> {
    const response = await apiClient.put<EmailAccount>("/api/v1/email/account", request);
    return response.data;
}

export async function deleteEmailAccount(): Promise<void> {
    await apiClient.delete("/api/v1/email/account");
}

export async function testEmailConnection(
    request: UpsertEmailAccountRequest
): Promise<ConnectionTestResponse> {
    const response = await apiClient.post<ConnectionTestResponse>(
        "/api/v1/email/account/test",
        request
    );
    return response.data;
}

export async function syncEmailInbox(): Promise<SyncEmailResponse> {
    const response = await apiClient.post<SyncEmailResponse>("/api/v1/email/sync");
    return response.data;
}

export async function fetchEmailSummary(): Promise<EmailSummary> {
    const response = await apiClient.get<EmailSummary>("/api/v1/email/summary");
    return response.data;
}

export async function fetchInboxMessages(category?: EmailCategory): Promise<InboxMessage[]> {
    const response = await apiClient.get<InboxMessage[]>("/api/v1/email/messages", {
        params: category ? { category } : undefined,
    });
    return response.data;
}

export async function fetchInboxMessage(id: string): Promise<InboxMessage> {
    const response = await apiClient.get<InboxMessage>(`/api/v1/email/messages/${id}`);
    return response.data;
}

export async function replyToEmail(
    messageId: string,
    request: ReplyToEmailRequest
): Promise<InboxMessage> {
    const response = await apiClient.post<InboxMessage>(
        `/api/v1/email/messages/${messageId}/reply`,
        request
    );
    return response.data;
}

export const EMAIL_PROVIDER_PRESETS = {
    gmail: {
        label: "Gmail",
        imapHost: "imap.gmail.com",
        imapPort: 993,
        smtpHost: "smtp.gmail.com",
        smtpPort: 587,
        useSsl: true,
    },
    outlook: {
        label: "Outlook / Office 365",
        imapHost: "outlook.office365.com",
        imapPort: 993,
        smtpHost: "smtp.office365.com",
        smtpPort: 587,
        useSsl: true,
    },
    yahoo: {
        label: "Yahoo Mail",
        imapHost: "imap.mail.yahoo.com",
        imapPort: 993,
        smtpHost: "smtp.mail.yahoo.com",
        smtpPort: 587,
        useSsl: true,
    },
} as const;

export const CATEGORY_LABELS: Record<EmailCategory, string> = {
    OFFER: "Offer",
    REJECTION: "Rejection",
    VACANCY: "Vacancy",
    REVISION_REQUEST: "Revision request",
    OTHER: "Other",
};

export const CATEGORY_TONES: Record<
    EmailCategory,
    "success" | "danger" | "info" | "warning" | "default"
> = {
    OFFER: "success",
    REJECTION: "danger",
    VACANCY: "info",
    REVISION_REQUEST: "warning",
    OTHER: "default",
};
