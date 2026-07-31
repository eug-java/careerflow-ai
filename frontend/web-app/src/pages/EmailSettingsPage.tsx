import { useState } from "react";
import { Link } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import {
    EMAIL_PROVIDER_PRESETS,
    deleteEmailAccount,
    fetchEmailAccount,
    testEmailConnection,
    upsertEmailAccount,
} from "../api/emailApi";
import { Card } from "../components/ui/Card";
import { useToast } from "../hooks/useToast";

export default function EmailSettingsPage() {
    const { pushToast } = useToast();
    const queryClient = useQueryClient();
    const accountQuery = useQuery({ queryKey: ["email-account"], queryFn: fetchEmailAccount });

    const account = accountQuery.data;
    const [password, setPassword] = useState("");
    const [emailAddress, setEmailAddress] = useState("");
    const [imapHost, setImapHost] = useState("");
    const [imapPort, setImapPort] = useState(993);
    const [smtpHost, setSmtpHost] = useState("");
    const [smtpPort, setSmtpPort] = useState(587);
    const [useSsl, setUseSsl] = useState(true);
    const [busy, setBusy] = useState<string | null>(null);

    const effectiveEmail = emailAddress || account?.emailAddress || "";
    const effectiveImapHost = imapHost || account?.imapHost || "imap.gmail.com";
    const effectiveImapPort = imapPort || account?.imapPort || 993;
    const effectiveSmtpHost = smtpHost || account?.smtpHost || "smtp.gmail.com";
    const effectiveSmtpPort = smtpPort || account?.smtpPort || 587;
    const effectiveUseSsl = account?.configured ? useSsl : useSsl;

    function applyPreset(key: keyof typeof EMAIL_PROVIDER_PRESETS) {
        const preset = EMAIL_PROVIDER_PRESETS[key];
        setImapHost(preset.imapHost);
        setImapPort(preset.imapPort);
        setSmtpHost(preset.smtpHost);
        setSmtpPort(preset.smtpPort);
        setUseSsl(preset.useSsl);
    }

    function buildRequest() {
        return {
            emailAddress: effectiveEmail,
            password,
            imapHost: effectiveImapHost,
            imapPort: effectiveImapPort,
            smtpHost: effectiveSmtpHost,
            smtpPort: effectiveSmtpPort,
            useSsl: effectiveUseSsl,
        };
    }

    async function handleSave() {
        if (!password) {
            pushToast("error", "Enter your password or app password to save settings.");
            return;
        }
        setBusy("save");
        try {
            await upsertEmailAccount(buildRequest());
            await queryClient.invalidateQueries({ queryKey: ["email-account"] });
            await queryClient.invalidateQueries({ queryKey: ["email-summary"] });
            setPassword("");
            pushToast("success", "Email account saved.");
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Save failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleTest() {
        setBusy("test");
        try {
            const result = await testEmailConnection(buildRequest());
            pushToast(result.success ? "success" : "error", result.message);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Test failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleDelete() {
        if (!window.confirm("Remove email integration?")) {
            return;
        }
        await deleteEmailAccount();
        await queryClient.invalidateQueries({ queryKey: ["email-account"] });
        pushToast("success", "Email account removed.");
    }

    return (
        <AppLayout>
            <div className="mb-8">
                <Link to="/email" className="text-sm text-indigo-600 hover:underline">
                    ← Back to inbox
                </Link>
                <h1 className="text-4xl font-bold mt-2">Email integration</h1>
                <p className="text-slate-500 mt-2">
                    Connect your mailbox to track recruiter emails and reply with generated documents.
                </p>
            </div>

            <Card className="max-w-3xl">
                {account?.configured && (
                    <p className="text-sm text-emerald-700 mb-4">
                        Connected as {account.emailAddress}. Enter password again to update or test.
                    </p>
                )}

                <div className="flex flex-wrap gap-2 mb-6">
                    {Object.entries(EMAIL_PROVIDER_PRESETS).map(([key, preset]) => (
                        <button
                            key={key}
                            type="button"
                            onClick={() => applyPreset(key as keyof typeof EMAIL_PROVIDER_PRESETS)}
                            className="border border-slate-300 px-3 py-1.5 rounded-lg text-sm hover:bg-slate-50"
                        >
                            {preset.label}
                        </button>
                    ))}
                </div>

                <div className="grid gap-4">
                    <input
                        type="email"
                        placeholder="Email address"
                        value={effectiveEmail}
                        onChange={(event) => setEmailAddress(event.target.value)}
                        className="border border-slate-300 rounded-xl px-4 py-2"
                    />
                    <input
                        type="password"
                        placeholder="Email password / app password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        className="border border-slate-300 rounded-xl px-4 py-2"
                    />
                    <div className="grid md:grid-cols-2 gap-4">
                        <input
                            placeholder="IMAP host"
                            value={effectiveImapHost}
                            onChange={(event) => setImapHost(event.target.value)}
                            className="border border-slate-300 rounded-xl px-4 py-2"
                        />
                        <input
                            type="number"
                            placeholder="IMAP port"
                            value={effectiveImapPort}
                            onChange={(event) => setImapPort(Number(event.target.value))}
                            className="border border-slate-300 rounded-xl px-4 py-2"
                        />
                        <input
                            placeholder="SMTP host"
                            value={effectiveSmtpHost}
                            onChange={(event) => setSmtpHost(event.target.value)}
                            className="border border-slate-300 rounded-xl px-4 py-2"
                        />
                        <input
                            type="number"
                            placeholder="SMTP port"
                            value={effectiveSmtpPort}
                            onChange={(event) => setSmtpPort(Number(event.target.value))}
                            className="border border-slate-300 rounded-xl px-4 py-2"
                        />
                    </div>
                    <label className="flex items-center gap-2 text-sm text-slate-600">
                        <input
                            type="checkbox"
                            checked={effectiveUseSsl}
                            onChange={(event) => setUseSsl(event.target.checked)}
                        />
                        Use SSL/TLS
                    </label>
                </div>

                <p className="text-sm text-slate-500 mt-6">
                    For Gmail and Outlook use an app-specific password with IMAP enabled.
                </p>

                <div className="flex flex-wrap gap-3 mt-6">
                    <button
                        onClick={handleSave}
                        disabled={busy !== null || !effectiveEmail}
                        className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700 disabled:opacity-50"
                    >
                        {busy === "save" ? "Saving..." : "Save account"}
                    </button>
                    <button
                        onClick={handleTest}
                        disabled={busy !== null || !effectiveEmail || !password}
                        className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50 disabled:opacity-50"
                    >
                        {busy === "test" ? "Testing..." : "Test connection"}
                    </button>
                    {account?.configured && (
                        <button
                            onClick={handleDelete}
                            className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                        >
                            Remove
                        </button>
                    )}
                </div>
            </Card>
        </AppLayout>
    );
}
