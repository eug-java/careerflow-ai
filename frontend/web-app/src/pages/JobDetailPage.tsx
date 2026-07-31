import { Link, useParams } from "react-router-dom";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import { fetchJobById } from "../api/jobApi";
import {
    useDocumentsQuery,
    useMatchesQuery,
    useProfilesQuery,
    useWorkflowsQuery,
} from "../hooks/useDashboardQueries";
import { Badge, Card, EmptyState, LoadingGrid, ScoreBar } from "../components/ui/Card";
import { calculateMatch } from "../api/matchingApi";
import { startDocumentGenerationWorkflow } from "../api/workflowApi";
import { computeSkillGap } from "../lib/dashboardUtils";
import { useEffect, useState } from "react";
import { useToast } from "../contexts/ToastContext";

export default function JobDetailPage() {
    const { id = "" } = useParams();
    const queryClient = useQueryClient();
    const { pushToast } = useToast();
    const [selectedProfileId, setSelectedProfileId] = useState("");
    const [busy, setBusy] = useState<string | null>(null);

    const jobQuery = useQuery({
        queryKey: ["job", id],
        queryFn: () => fetchJobById(id),
        enabled: Boolean(id),
    });

    const profilesQuery = useProfilesQuery();
    const matchesQuery = useMatchesQuery(undefined, id);
    const documentsQuery = useDocumentsQuery(undefined, id);
    const workflowsQuery = useWorkflowsQuery();

    const profiles = profilesQuery.data ?? [];
    const matches = matchesQuery.data ?? [];
    const documents = documentsQuery.data ?? [];
    const workflows = workflowsQuery.data ?? [];

    useEffect(() => {
        if (!selectedProfileId && profiles[0]) {
            setSelectedProfileId(profiles[0].id);
        }
    }, [profiles, selectedProfileId]);

    if (jobQuery.isLoading) {
        return (
            <AppLayout>
                <LoadingGrid count={2} />
            </AppLayout>
        );
    }

    if (jobQuery.isError || !jobQuery.data) {
        return (
            <AppLayout>
                <EmptyState
                    title="Job not found"
                    description="This job may have been deleted."
                    actionLabel="Back to jobs"
                    actionHref="/jobs"
                />
            </AppLayout>
        );
    }

    const job = jobQuery.data;
    const profile = profiles.find((item) => item.id === selectedProfileId);
    const gap = computeSkillGap(profile, job);
    const jobMatches = [...matches].sort(
        (a, b) => Number(b.totalScore) - Number(a.totalScore)
    );

    async function handleMatch() {
        if (!selectedProfileId) {
            pushToast("error", "Select a profile first.");
            return;
        }

        setBusy("match");
        try {
            const result = await calculateMatch(selectedProfileId, id);
            await queryClient.invalidateQueries({ queryKey: ["matches"] });
            pushToast("success", `Match: ${Number(result.totalScore).toFixed(0)}%`);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Match failed");
        } finally {
            setBusy(null);
        }
    }

    async function handleGenerate(documentType: "COVER_LETTER" | "RESUME") {
        if (!selectedProfileId) {
            pushToast("error", "Select a profile first.");
            return;
        }

        setBusy(documentType);
        try {
            await startDocumentGenerationWorkflow({
                profileId: selectedProfileId,
                jobId: id,
                documentType,
            });
            await queryClient.invalidateQueries({ queryKey: ["workflows"] });
            pushToast("success", `${documentType.replace("_", " ")} generation started.`);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Generation failed");
        } finally {
            setBusy(null);
        }
    }

    return (
        <AppLayout>
            <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-8">
                <div>
                    <Link to="/jobs" className="text-sm text-indigo-600 hover:underline">
                        ← Back to jobs
                    </Link>
                    <h1 className="text-4xl font-bold mt-2">{job.title}</h1>
                    <p className="text-slate-500 text-lg">
                        {job.companyName} · {job.location}
                        {job.remote && " · Remote"}
                    </p>
                    <p className="text-sm text-slate-400 mt-2">
                        {job.employmentType} · {job.currency} {job.salaryMin}–{job.salaryMax}
                    </p>
                </div>
                <Link
                    to={`/jobs/${job.id}/edit`}
                    className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50 h-fit"
                >
                    Edit job
                </Link>
            </div>

            <div className="grid xl:grid-cols-3 gap-6 mb-8">
                <Card className="xl:col-span-2">
                    <h2 className="text-xl font-semibold mb-4">Description</h2>
                    <p className="text-slate-700 whitespace-pre-wrap">{job.description}</p>
                    <div className="mt-6">
                        <h3 className="font-medium mb-2">Required skills</h3>
                        <div className="flex flex-wrap gap-2">
                            {(job.skills ?? []).map((skill) => (
                                <Badge key={skill.name} tone={skill.required ? "warning" : "default"}>
                                    {skill.name}
                                    {skill.required ? " *" : ""}
                                </Badge>
                            ))}
                        </div>
                    </div>
                </Card>

                <Card>
                    <h2 className="text-xl font-semibold mb-4">Actions</h2>
                    <label className="text-sm text-slate-600 block mb-2">Profile</label>
                    <select
                        value={selectedProfileId}
                        onChange={(event) => setSelectedProfileId(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-3 py-2 mb-4"
                    >
                        {profiles.map((item) => (
                            <option key={item.id} value={item.id}>
                                {item.fullName}
                            </option>
                        ))}
                    </select>
                    <button
                        onClick={handleMatch}
                        disabled={busy !== null}
                        className="w-full bg-indigo-600 text-white rounded-xl py-2 mb-2 hover:bg-indigo-500 disabled:opacity-50"
                    >
                        {busy === "match" ? "Calculating..." : "Calculate match"}
                    </button>
                    <button
                        onClick={() => handleGenerate("COVER_LETTER")}
                        disabled={busy !== null}
                        className="w-full bg-slate-900 text-white rounded-xl py-2 mb-2 hover:bg-slate-700 disabled:opacity-50"
                    >
                        Generate cover letter
                    </button>
                    <button
                        onClick={() => handleGenerate("RESUME")}
                        disabled={busy !== null}
                        className="w-full border border-slate-300 rounded-xl py-2 hover:bg-slate-50 disabled:opacity-50"
                    >
                        Generate resume
                    </button>
                </Card>
            </div>

            {profile && (
                <Card className="mb-8">
                    <h2 className="text-xl font-semibold mb-4">Skill gap — {profile.fullName}</h2>
                    <div className="grid md:grid-cols-2 gap-6">
                        <div>
                            <p className="font-medium text-emerald-700 mb-2">Matched</p>
                            <p className="text-slate-600">
                                {gap.matched.length > 0 ? gap.matched.join(", ") : "No required skills matched yet."}
                            </p>
                        </div>
                        <div>
                            <p className="font-medium text-rose-700 mb-2">Missing</p>
                            <p className="text-slate-600">
                                {gap.missing.length > 0 ? gap.missing.join(", ") : "All required skills covered."}
                            </p>
                        </div>
                    </div>
                </Card>
            )}

            <div className="grid xl:grid-cols-2 gap-6">
                <Card>
                    <h2 className="text-xl font-semibold mb-4">Match history</h2>
                    {jobMatches.length === 0 ? (
                        <p className="text-slate-500">No matches for this job yet.</p>
                    ) : (
                        <div className="space-y-4">
                            {jobMatches.map((match) => {
                                const matchProfile = profiles.find(
                                    (item) => item.id === match.profileId
                                );
                                return (
                                    <div key={match.id} className="border border-slate-100 rounded-xl p-4">
                                        <div className="flex justify-between">
                                            <p className="font-medium">{matchProfile?.fullName}</p>
                                            <span className="font-bold text-indigo-600">
                                                {Number(match.totalScore).toFixed(0)}%
                                            </span>
                                        </div>
                                        <div className="grid grid-cols-3 gap-2 mt-3">
                                            <ScoreBar label="Skills" score={Number(match.skillsScore)} />
                                            <ScoreBar label="Loc" score={Number(match.locationScore)} />
                                            <ScoreBar label="Sal" score={Number(match.salaryScore)} />
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    )}
                </Card>

                <Card>
                    <h2 className="text-xl font-semibold mb-4">Documents & workflows</h2>
                    <p className="text-sm text-slate-500 mb-3">{documents.length} documents generated</p>
                    {documents.slice(0, 5).map((doc) => (
                        <div key={doc.id} className="border-b border-slate-100 py-2 last:border-0">
                            <Badge>{doc.documentType.replace("_", " ")}</Badge>
                            <p className="text-sm mt-1">{doc.fileName}</p>
                        </div>
                    ))}
                    <p className="text-sm text-slate-500 mt-4 mb-2">
                        {workflows.filter((item) => item.status === "RUNNING").length} running workflows
                    </p>
                    <Link to="/documents" className="text-sm text-indigo-600 hover:underline">
                        Open document library
                    </Link>
                </Card>
            </div>
        </AppLayout>
    );
}
