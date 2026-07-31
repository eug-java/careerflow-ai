import { useState } from "react";
import { Link } from "react-router-dom";
import { useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import { deleteJob } from "../api/jobApi";
import { calculateMatch } from "../api/matchingApi";
import { startDocumentGenerationWorkflow } from "../api/workflowApi";
import { connectWorkflowStatusSocket } from "../api/workflowSocket";
import { useJobsQuery, useProfilesQuery, useMatchesQuery } from "../hooks/useDashboardQueries";
import { Badge, EmptyState, LoadingGrid, ScoreBar } from "../components/ui/Card";
import { useToast } from "../hooks/useToast";
import { useEffect } from "react";

export default function JobsPage() {
    const { data: jobs = [], isLoading: jobsLoading } = useJobsQuery();
    const { data: profiles = [], isLoading: profilesLoading } = useProfilesQuery();
    const { data: matches = [] } = useMatchesQuery();
    const queryClient = useQueryClient();
    const { pushToast } = useToast();

    const [selectedProfileId, setSelectedProfileId] = useState("");
    const [workflowStatus, setWorkflowStatus] = useState<string>("");
    const [currentProcessInstanceKey, setCurrentProcessInstanceKey] = useState<number | null>(null);

    const activeProfileId = selectedProfileId || profiles[0]?.id || "";

    useEffect(() => {
        if (!currentProcessInstanceKey) {
            return;
        }

        const socket = connectWorkflowStatusSocket(currentProcessInstanceKey, (statusMessage) => {
            setWorkflowStatus(statusMessage.status);
            if (statusMessage.status === "COMPLETED") {
                pushToast("success", "Document generation completed.");
                void queryClient.invalidateQueries({ queryKey: ["documents"] });
                void queryClient.invalidateQueries({ queryKey: ["workflows"] });
            }
            if (statusMessage.status === "FAILED") {
                pushToast("error", statusMessage.message || "Workflow failed.");
            }
        });

        return () => {
            socket.close();
        };
    }, [currentProcessInstanceKey, pushToast, queryClient]);

    function getMatchForJob(jobId: string) {
        if (!activeProfileId) {
            return undefined;
        }
        return matches.find(
            (match) => match.jobId === jobId && match.profileId === activeProfileId
        );
    }

    async function handleDeleteJob(jobId: string) {
        const confirmed = window.confirm("Delete this job?");
        if (!confirmed) {
            return;
        }
        await deleteJob(jobId);
        await queryClient.invalidateQueries({ queryKey: ["jobs"] });
    }

    async function handleMatch(jobId: string) {
        if (!activeProfileId) {
            pushToast("error", "Please select a profile first.");
            return;
        }
        try {
            const result = await calculateMatch(activeProfileId, jobId);
            await queryClient.invalidateQueries({ queryKey: ["matches"] });
            pushToast("success", `Match: ${Number(result.totalScore).toFixed(0)}%`);
        } catch (err) {
            pushToast("error", err instanceof Error ? err.message : "Match failed");
        }
    }

    async function handleGenerate(jobId: string, documentType: "COVER_LETTER" | "RESUME") {
        if (!activeProfileId) {
            pushToast("error", "Please select a profile first.");
            return;
        }
        try {
            const workflow = await startDocumentGenerationWorkflow({
                profileId: activeProfileId,
                jobId,
                documentType,
            });
            setCurrentProcessInstanceKey(workflow.processInstanceKey);
            setWorkflowStatus("RUNNING");
            await queryClient.invalidateQueries({ queryKey: ["workflows"] });
            pushToast("info", "Document generation started.");
        } catch (err) {
            pushToast("error", err instanceof Error ? err.message : "Workflow failed");
        }
    }

    const isLoading = jobsLoading || profilesLoading;

    return (
        <AppLayout>
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-4xl font-bold">Jobs</h1>
                    <p className="text-slate-500 mt-2">Track vacancies, calculate matches, and generate documents.</p>
                </div>
                <Link
                    to="/jobs/new"
                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                >
                    Add Job
                </Link>
            </div>

            {workflowStatus && (
                <div className="mb-6 bg-blue-50 text-blue-800 px-4 py-3 rounded-xl">
                    Workflow status: {workflowStatus}
                </div>
            )}

            <div className="mb-6 bg-white rounded-2xl shadow p-6">
                <label className="block text-sm font-medium text-slate-700 mb-2">
                    Active profile for matching & generation
                </label>
                <select
                    value={activeProfileId}
                    onChange={(event) => setSelectedProfileId(event.target.value)}
                    className="w-full border border-slate-300 rounded-xl px-4 py-2"
                >
                    {profiles.map((profile) => (
                        <option key={profile.id} value={profile.id}>
                            {profile.fullName} - {profile.professionalTitle}
                        </option>
                    ))}
                </select>
            </div>

            {isLoading ? (
                <LoadingGrid count={3} />
            ) : jobs.length === 0 ? (
                <EmptyState
                    title="No jobs yet"
                    description="Add a job manually or paste a description on the dashboard."
                    actionLabel="Add job"
                    actionHref="/jobs/new"
                />
            ) : (
                <div className="grid gap-6">
                    {jobs.map((job) => {
                        const match = getMatchForJob(job.id);
                        return (
                            <div key={job.id} className="bg-white rounded-2xl shadow p-6">
                                <div className="flex flex-col lg:flex-row lg:justify-between gap-6">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-3">
                                            <Link
                                                to={`/jobs/${job.id}`}
                                                className="text-2xl font-semibold hover:text-indigo-600"
                                            >
                                                {job.title}
                                            </Link>
                                            {match && (
                                                <Badge tone={Number(match.totalScore) >= 70 ? "success" : "warning"}>
                                                    {Number(match.totalScore).toFixed(0)}% match
                                                </Badge>
                                            )}
                                        </div>
                                        <p className="text-slate-500">
                                            {job.companyName} · {job.location}
                                        </p>
                                        <p className="mt-4 text-slate-700 line-clamp-3">{job.description}</p>
                                        {match && (
                                            <div className="grid md:grid-cols-3 gap-3 mt-4 max-w-xl">
                                                <ScoreBar label="Skills" score={Number(match.skillsScore)} />
                                                <ScoreBar label="Location" score={Number(match.locationScore)} />
                                                <ScoreBar label="Salary" score={Number(match.salaryScore)} />
                                            </div>
                                        )}
                                    </div>
                                    <div className="flex flex-col gap-2 min-w-48">
                                        <button
                                            onClick={() => handleMatch(job.id)}
                                            className="bg-indigo-600 text-white px-4 py-2 rounded-xl hover:bg-indigo-500"
                                        >
                                            Calculate match
                                        </button>
                                        <button
                                            onClick={() => handleGenerate(job.id, "COVER_LETTER")}
                                            className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                                        >
                                            Cover letter
                                        </button>
                                        <button
                                            onClick={() => handleGenerate(job.id, "RESUME")}
                                            className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                        >
                                            Resume
                                        </button>
                                        <Link
                                            to={`/jobs/${job.id}`}
                                            className="text-center border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                        >
                                            Details
                                        </Link>
                                        <Link
                                            to={`/jobs/${job.id}/edit`}
                                            className="text-center border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                        >
                                            Edit
                                        </Link>
                                        <button
                                            onClick={() => handleDeleteJob(job.id)}
                                            className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                                        >
                                            Delete
                                        </button>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </AppLayout>
    );
}
