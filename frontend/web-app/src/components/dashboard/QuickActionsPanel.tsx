import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { calculateMatch } from "../../api/matchingApi";
import { createJob, parseJobDescriptionWithAi } from "../../api/jobApi";
import type { Job } from "../../api/jobApi";
import type { Profile } from "../../api/profileApi";
import { startDocumentGenerationWorkflow } from "../../api/workflowApi";
import { Card } from "../ui/Card";
import { useToast } from "../../contexts/ToastContext";

interface Props {
    profiles: Profile[];
    jobs: Job[];
}

export function QuickActionsPanel({ profiles, jobs }: Props) {
    const { pushToast } = useToast();
    const queryClient = useQueryClient();
    const [jdText, setJdText] = useState("");
    const [profileId, setProfileId] = useState(profiles[0]?.id ?? "");
    const [jobId, setJobId] = useState(jobs[0]?.id ?? "");
    const [busy, setBusy] = useState<string | null>(null);

    async function handleParseAndCreate() {
        if (!jdText.trim()) {
            pushToast("error", "Paste a job description first.");
            return;
        }

        setBusy("parse");
        try {
            const parsed = await parseJobDescriptionWithAi({ text: jdText });
            await createJob({
                title: parsed.title,
                companyName: parsed.companyName,
                location: parsed.location,
                employmentType: parsed.employmentType,
                salaryMin: parsed.salaryMin ?? 0,
                salaryMax: parsed.salaryMax ?? 0,
                currency: parsed.currency,
                remote: parsed.remote,
                description: parsed.description,
                skills: parsed.skills,
            });
            await queryClient.invalidateQueries({ queryKey: ["jobs"] });
            setJdText("");
            pushToast("success", "Job created from pasted description.");
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Parse failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleQuickMatch() {
        if (!profileId || !jobId) {
            pushToast("error", "Select profile and job.");
            return;
        }

        setBusy("match");
        try {
            const result = await calculateMatch(profileId, jobId);
            await queryClient.invalidateQueries({ queryKey: ["matches"] });
            pushToast("success", `Match calculated: ${Number(result.totalScore).toFixed(0)}%`);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Match failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleQuickGenerate(documentType: "COVER_LETTER" | "RESUME") {
        if (!profileId || !jobId) {
            pushToast("error", "Select profile and job.");
            return;
        }

        setBusy(documentType);
        try {
            await startDocumentGenerationWorkflow({ profileId, jobId, documentType });
            await queryClient.invalidateQueries({ queryKey: ["workflows"] });
            pushToast("success", `${documentType.replace("_", " ")} generation started.`);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Workflow failed");
        } finally {
            setBusy(null);
        }
    }

    return (
        <Card>
            <h2 className="text-xl font-semibold mb-4">Quick actions</h2>

            <div className="space-y-4">
                <div>
                    <label className="text-sm text-slate-600 block mb-2">Paste job description</label>
                    <textarea
                        value={jdText}
                        onChange={(event) => setJdText(event.target.value)}
                        rows={4}
                        className="w-full border border-slate-300 rounded-xl px-3 py-2 text-sm"
                        placeholder="Paste a job posting to create a job with AI..."
                    />
                    <button
                        onClick={handleParseAndCreate}
                        disabled={busy !== null}
                        className="mt-2 w-full bg-slate-900 text-white rounded-xl py-2 text-sm hover:bg-slate-700 disabled:opacity-50"
                    >
                        {busy === "parse" ? "Parsing..." : "Parse & create job"}
                    </button>
                </div>

                <div>
                    <label className="text-sm text-slate-600 block mb-2">Profile</label>
                    <select
                        value={profileId}
                        onChange={(event) => setProfileId(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-3 py-2 text-sm"
                    >
                        {profiles.map((profile) => (
                            <option key={profile.id} value={profile.id}>
                                {profile.fullName}
                            </option>
                        ))}
                    </select>
                </div>

                <div>
                    <label className="text-sm text-slate-600 block mb-2">Job</label>
                    <select
                        value={jobId}
                        onChange={(event) => setJobId(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-3 py-2 text-sm"
                    >
                        {jobs.map((job) => (
                            <option key={job.id} value={job.id}>
                                {job.title} — {job.companyName}
                            </option>
                        ))}
                    </select>
                </div>

                <button
                    onClick={handleQuickMatch}
                    disabled={busy !== null || !profileId || !jobId}
                    className="w-full bg-indigo-600 text-white rounded-xl py-2 text-sm hover:bg-indigo-500 disabled:opacity-50"
                >
                    {busy === "match" ? "Calculating..." : "Quick match"}
                </button>

                <div className="grid grid-cols-2 gap-2">
                    <button
                        onClick={() => handleQuickGenerate("COVER_LETTER")}
                        disabled={busy !== null || !profileId || !jobId}
                        className="bg-white border border-slate-300 rounded-xl py-2 text-sm hover:bg-slate-50 disabled:opacity-50"
                    >
                        Cover letter
                    </button>
                    <button
                        onClick={() => handleQuickGenerate("RESUME")}
                        disabled={busy !== null || !profileId || !jobId}
                        className="bg-white border border-slate-300 rounded-xl py-2 text-sm hover:bg-slate-50 disabled:opacity-50"
                    >
                        Resume
                    </button>
                </div>

                <Link to="/jobs/new" className="block text-center text-sm text-indigo-600 hover:underline">
                    Open full job form
                </Link>
            </div>
        </Card>
    );
}
