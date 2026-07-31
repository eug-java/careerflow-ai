import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import {
    CATEGORY_LABELS,
    CATEGORY_TONES,
    type EmailCategory,
    fetchEmailSummary,
    fetchInboxMessages,
    replyToEmail,
    syncEmailInbox,
} from "../api/emailApi";
import { fetchDocuments } from "../api/documentApi";
import { Badge, Card, EmptyState, LoadingGrid } from "../components/ui/Card";
import { formatRelativeTime } from "../lib/dashboardUtils";
import { useToast } from "../hooks/useToast";

const FILTER_OPTIONS: Array<EmailCategory | "ALL"> = [
    "ALL",
    "OFFER",
    "REJECTION",
    "VACANCY",
    "REVISION_REQUEST",
    "OTHER",
];

export default function EmailInboxPage() {
    const { pushToast } = useToast();
    const queryClient = useQueryClient();
    const [categoryFilter, setCategoryFilter] = useState<EmailCategory | "ALL">("ALL");
    const [selectedMessageId, setSelectedMessageId] = useState<string | null>(null);
    const [replyBody, setReplyBody] = useState(
        "Hello,\n\nPlease find my updated application documents attached.\n\nBest regards"
    );
    const [selectedDocumentIds, setSelectedDocumentIds] = useState<string[]>([]);
    const [busy, setBusy] = useState<string | null>(null);

    const summaryQuery = useQuery({ queryKey: ["email-summary"], queryFn: fetchEmailSummary });
    const messagesQuery = useQuery({
        queryKey: ["email-messages", categoryFilter],
        queryFn: () =>
            fetchInboxMessages(categoryFilter === "ALL" ? undefined : categoryFilter),
        enabled: summaryQuery.data?.accountConfigured ?? false,
    });
    const documentsQuery = useQuery({ queryKey: ["documents"], queryFn: () => fetchDocuments() });

    const messages = messagesQuery.data ?? [];
    const documents = documentsQuery.data ?? [];
    const selectedMessage =
        messages.find((message) => message.id === selectedMessageId) ?? null;

    async function handleSync() {
        setBusy("sync");
        try {
            const result = await syncEmailInbox();
            await queryClient.invalidateQueries({ queryKey: ["email-messages"] });
            await queryClient.invalidateQueries({ queryKey: ["email-summary"] });
            pushToast("success", `Synced ${result.importedCount} new emails.`);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Sync failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleReply() {
        if (!selectedMessage) {
            return;
        }
        if (selectedDocumentIds.length === 0) {
            pushToast("error", "Select at least one document to attach.");
            return;
        }
        setBusy("reply");
        try {
            await replyToEmail(selectedMessage.id, {
                documentIds: selectedDocumentIds,
                bodyText: replyBody,
            });
            await queryClient.invalidateQueries({ queryKey: ["email-messages"] });
            pushToast("success", "Reply sent with attachments.");
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Reply failed");
        } finally {
            setBusy(null);
        }
    }

    function toggleDocument(documentId: string) {
        setSelectedDocumentIds((current) =>
            current.includes(documentId)
                ? current.filter((id) => id !== documentId)
                : [...current, documentId]
        );
    }

    if (summaryQuery.isLoading) {
        return (
            <AppLayout>
                <LoadingGrid count={2} />
            </AppLayout>
        );
    }

    if (!summaryQuery.data?.accountConfigured) {
        return (
            <AppLayout>
                <EmptyState
                    title="Email not connected"
                    description="Add your mailbox credentials to sync recruiter emails and send replies with resume or cover letter."
                    actionLabel="Configure email"
                    actionHref="/email/settings"
                />
            </AppLayout>
        );
    }

    return (
        <AppLayout>
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4 mb-8">
                <div>
                    <h1 className="text-4xl font-bold">Recruiter inbox</h1>
                    <p className="text-slate-500 mt-2">
                        Track offers, rejections, vacancies, and revision requests from your mailbox.
                    </p>
                </div>
                <div className="flex flex-wrap gap-3">
                    <Link
                        to="/email/settings"
                        className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                    >
                        Settings
                    </Link>
                    <button
                        onClick={handleSync}
                        disabled={busy !== null}
                        className="bg-indigo-600 text-white px-4 py-2 rounded-xl hover:bg-indigo-500 disabled:opacity-50"
                    >
                        {busy === "sync" ? "Syncing..." : "Sync inbox"}
                    </button>
                </div>
            </div>

            <div className="grid md:grid-cols-5 gap-4 mb-6">
                {FILTER_OPTIONS.map((filter) => (
                    <button
                        key={filter}
                        onClick={() => setCategoryFilter(filter)}
                        className={`rounded-xl px-4 py-3 text-left border ${
                            categoryFilter === filter
                                ? "border-indigo-500 bg-indigo-50"
                                : "border-slate-200 bg-white"
                        }`}
                    >
                        <p className="text-sm text-slate-500">{filter === "ALL" ? "All" : CATEGORY_LABELS[filter]}</p>
                        <p className="text-2xl font-bold">
                            {filter === "ALL"
                                ? summaryQuery.data?.totalMessages ?? 0
                                : summaryQuery.data?.byCategory?.[filter] ?? 0}
                        </p>
                    </button>
                ))}
            </div>

            <div className="grid xl:grid-cols-2 gap-6">
                <Card>
                    <h2 className="text-xl font-semibold mb-4">Messages</h2>
                    {messagesQuery.isLoading ? (
                        <LoadingGrid count={2} />
                    ) : messages.length === 0 ? (
                        <p className="text-slate-500">No messages yet. Click Sync inbox.</p>
                    ) : (
                        <div className="space-y-3 max-h-[700px] overflow-auto">
                            {messages.map((message) => (
                                <button
                                    key={message.id}
                                    type="button"
                                    onClick={() => setSelectedMessageId(message.id)}
                                    className={`w-full text-left border rounded-xl p-4 ${
                                        selectedMessageId === message.id
                                            ? "border-indigo-500 bg-indigo-50"
                                            : "border-slate-100"
                                    }`}
                                >
                                    <div className="flex items-center justify-between gap-2">
                                        <Badge tone={CATEGORY_TONES[message.category]}>
                                            {CATEGORY_LABELS[message.category]}
                                        </Badge>
                                        <span className="text-xs text-slate-400">
                                            {formatRelativeTime(message.receivedAt)}
                                        </span>
                                    </div>
                                    <p className="font-medium mt-2">{message.subject || "(No subject)"}</p>
                                    <p className="text-sm text-slate-500">{message.fromAddress}</p>
                                    {message.replied && (
                                        <p className="text-xs text-emerald-600 mt-2">Replied</p>
                                    )}
                                </button>
                            ))}
                        </div>
                    )}
                </Card>

                <Card>
                    <h2 className="text-xl font-semibold mb-4">Message details & reply</h2>
                    {!selectedMessage ? (
                        <p className="text-slate-500">Select a message to preview and reply.</p>
                    ) : (
                        <div className="space-y-4">
                            <div>
                                <p className="font-semibold text-lg">
                                    {selectedMessage.subject || "(No subject)"}
                                </p>
                                <p className="text-sm text-slate-500">{selectedMessage.fromAddress}</p>
                                <p className="text-xs text-slate-400 mt-1">
                                    {selectedMessage.classificationReason}
                                </p>
                            </div>
                            <pre className="whitespace-pre-wrap text-sm bg-slate-50 rounded-xl p-4 max-h-56 overflow-auto">
                                {selectedMessage.bodyText || selectedMessage.bodyPreview}
                            </pre>

                            <div>
                                <p className="text-sm font-medium mb-2">Attach generated documents</p>
                                <div className="space-y-2 max-h-40 overflow-auto">
                                    {documents.map((doc) => (
                                        <label
                                            key={doc.id}
                                            className="flex items-center gap-2 text-sm border border-slate-100 rounded-lg px-3 py-2"
                                        >
                                            <input
                                                type="checkbox"
                                                checked={selectedDocumentIds.includes(doc.id)}
                                                onChange={() => toggleDocument(doc.id)}
                                            />
                                            {doc.documentType.replace("_", " ")} — {doc.fileName}
                                        </label>
                                    ))}
                                </div>
                            </div>

                            <textarea
                                value={replyBody}
                                onChange={(event) => setReplyBody(event.target.value)}
                                rows={6}
                                className="w-full border border-slate-300 rounded-xl px-3 py-2 text-sm"
                            />

                            <button
                                onClick={handleReply}
                                disabled={busy !== null || selectedMessage.replied}
                                className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700 disabled:opacity-50"
                            >
                                {busy === "reply"
                                    ? "Sending..."
                                    : selectedMessage.replied
                                      ? "Already replied"
                                      : "Send reply with PDF attachments"}
                            </button>
                        </div>
                    )}
                </Card>
            </div>
        </AppLayout>
    );
}
