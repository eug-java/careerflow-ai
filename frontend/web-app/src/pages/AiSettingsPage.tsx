import { useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import {
    AI_MODEL_OPTIONS,
    deleteAiAccount,
    fetchAiAccount,
    testAiConnection,
    upsertAiAccount,
} from "../api/aiApi";
import { Card } from "../components/ui/Card";
import { useToast } from "../hooks/useToast";

export default function AiSettingsPage() {
    const { pushToast } = useToast();
    const queryClient = useQueryClient();
    const accountQuery = useQuery({ queryKey: ["ai-account"], queryFn: fetchAiAccount });

    const account = accountQuery.data;
    const [apiKey, setApiKey] = useState("");
    const [preferredModel, setPreferredModel] = useState("gpt-4o-mini");
    const [busy, setBusy] = useState<string | null>(null);

    const effectiveModel = account?.configured ? account.preferredModel : preferredModel;

    async function handleSave() {
        if (!apiKey) {
            pushToast("error", "Enter your OpenAI API key to save settings.");
            return;
        }
        setBusy("save");
        try {
            await upsertAiAccount({
                apiKey,
                provider: "openai",
                preferredModel: effectiveModel,
            });
            await queryClient.invalidateQueries({ queryKey: ["ai-account"] });
            setApiKey("");
            pushToast("success", "AI settings saved.");
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Save failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleTest() {
        if (!apiKey) {
            pushToast("error", "Enter your OpenAI API key to test the connection.");
            return;
        }
        setBusy("test");
        try {
            const result = await testAiConnection({
                apiKey,
                provider: "openai",
                preferredModel: effectiveModel,
            });
            pushToast(result.success ? "success" : "error", result.message);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Test failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleDelete() {
        if (!window.confirm("Remove your saved OpenAI API key?")) {
            return;
        }
        await deleteAiAccount();
        await queryClient.invalidateQueries({ queryKey: ["ai-account"] });
        pushToast("success", "AI settings removed.");
    }

    return (
        <AppLayout>
            <div className="mb-8">
                <h1 className="text-4xl font-bold">AI settings</h1>
                <p className="text-slate-500 mt-2">
                    Store your OpenAI API key in your account to generate resumes, cover letters, and
                    parse job descriptions.
                </p>
            </div>

            <Card className="max-w-3xl">
                {account?.configured && (
                    <p className="text-sm text-emerald-700 mb-4">
                        Key configured ({account.apiKeyHint}). Enter a new key to update or test.
                    </p>
                )}

                <div className="grid gap-4">
                    <input
                        type="password"
                        placeholder="OpenAI API key (sk-...)"
                        value={apiKey}
                        onChange={(event) => setApiKey(event.target.value)}
                        className="border border-slate-300 rounded-xl px-4 py-2"
                    />
                    <select
                        value={effectiveModel}
                        onChange={(event) => setPreferredModel(event.target.value)}
                        className="border border-slate-300 rounded-xl px-4 py-2"
                    >
                        {AI_MODEL_OPTIONS.map((option) => (
                            <option key={option.value} value={option.value}>
                                {option.label}
                            </option>
                        ))}
                    </select>
                </div>

                <p className="text-sm text-slate-500 mt-6">
                    Your key is encrypted at rest and never returned to the browser after saving.
                </p>

                <div className="flex flex-wrap gap-3 mt-6">
                    <button
                        onClick={handleSave}
                        disabled={busy !== null}
                        className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700 disabled:opacity-50"
                    >
                        {busy === "save" ? "Saving..." : "Save settings"}
                    </button>
                    <button
                        onClick={handleTest}
                        disabled={busy !== null || !apiKey}
                        className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50 disabled:opacity-50"
                    >
                        {busy === "test" ? "Testing..." : "Test connection"}
                    </button>
                    {account?.configured && (
                        <button
                            onClick={handleDelete}
                            className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                        >
                            Remove key
                        </button>
                    )}
                </div>
            </Card>
        </AppLayout>
    );
}
