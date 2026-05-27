import { useEffect, useState } from "react";
import AppLayout from "../layouts/AppLayout";

import { deleteJob, fetchJobs } from "../api/jobApi";
import type { Job } from "../api/jobApi";

import { fetchProfiles } from "../api/profileApi";
import type { Profile } from "../api/profileApi";

import { Link } from "react-router-dom";

import {
    startDocumentGenerationWorkflow,
    fetchWorkflowStatus,
} from "../api/workflowApi";

import { connectWorkflowStatusSocket } from "../api/workflowSocket";

export default function JobsPage() {
    const [jobs, setJobs] = useState<Job[]>([]);
    const [profiles, setProfiles] = useState<Profile[]>([]);

    const [selectedProfileId, setSelectedProfileId] = useState<string>("");

    const [message, setMessage] = useState<string>("");
    const [workflowStatus, setWorkflowStatus] = useState<string>("");

    const [currentProcessInstanceKey, setCurrentProcessInstanceKey] =
        useState<number | null>(null);

    useEffect(() => {
        fetchJobs().then(setJobs);

        fetchProfiles().then((data) => {
            setProfiles(data);

            if (data.length > 0) {
                setSelectedProfileId(data[0].id);
            }
        });
    }, []);

    useEffect(() => {
        const socket = connectWorkflowStatusSocket((message) => {
            if (
                currentProcessInstanceKey &&
                message.processInstanceKey === currentProcessInstanceKey
            ) {
                setWorkflowStatus(message.status);
            }
        });

        return () => {
            socket.close();
        };
    }, [currentProcessInstanceKey]);

    async function handleDeleteJob(jobId: string) {
        const confirmed = window.confirm("Delete this job?");

        if (!confirmed) {
            return;
        }

        await deleteJob(jobId);

        setJobs((current) =>
            current.filter((job) => job.id !== jobId)
        );
    }

    async function handleGenerate(
        jobId: string,
        documentType: "COVER_LETTER" | "RESUME"
    ) {
        if (!selectedProfileId) {
            setMessage("Please select a profile first.");
            return;
        }

        const workflow = await startDocumentGenerationWorkflow({
            profileId: selectedProfileId,
            jobId,
            documentType,
        });

        const processInstanceKey = workflow.processInstanceKey;

        setCurrentProcessInstanceKey(processInstanceKey);
        setMessage(`Workflow started: ${processInstanceKey}`);
        setWorkflowStatus("RUNNING");

        const interval = setInterval(async () => {
            try {
                const status = await fetchWorkflowStatus(processInstanceKey);

                setWorkflowStatus(status.status);

                if (status.status === "COMPLETED" || status.status === "FAILED") {
                    clearInterval(interval);
                }
            } catch (error) {
                console.error(error);
                clearInterval(interval);
            }
        }, 2000);
    }

    return (
        <AppLayout>
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-4xl font-bold">Jobs</h1>

                <div className="flex flex-col gap-3 items-end">
                    <Link
                        to="/jobs/new"
                        className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                    >
                        Add Job
                    </Link>

                    {message && (
                        <div className="bg-green-100 text-green-800 px-4 py-2 rounded-xl">
                            {message}
                        </div>
                    )}

                    {workflowStatus && (
                        <div className="bg-blue-100 text-blue-800 px-4 py-2 rounded-xl">
                            Workflow Status: {workflowStatus}
                        </div>
                    )}
                </div>
            </div>

            <div className="mb-6 bg-white rounded-2xl shadow p-6">
                <label className="block text-sm font-medium text-slate-700 mb-2">
                    Select profile for document generation
                </label>

                <select
                    value={selectedProfileId}
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

            <div className="grid gap-6">
                {jobs.map((job) => (
                    <div key={job.id} className="bg-white rounded-2xl shadow p-6">
                        <div className="flex justify-between gap-6">
                            <div>
                                <h2 className="text-2xl font-semibold">{job.title}</h2>

                                <p className="text-slate-500">
                                    {job.companyName} · {job.location}
                                </p>

                                <p className="mt-4 text-slate-700">{job.description}</p>

                                <p className="mt-4 text-sm text-slate-500">
                                    {job.employmentType} · {job.currency} {job.salaryMin} -{" "}
                                    {job.salaryMax}
                                </p>
                            </div>

                            <div className="flex flex-col gap-3 min-w-48">
                                <button
                                    onClick={() => handleGenerate(job.id, "COVER_LETTER")}
                                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                                >
                                    Generate Cover Letter
                                </button>

                                <button
                                    onClick={() => handleGenerate(job.id, "RESUME")}
                                    className="bg-white border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-100"
                                >
                                    Generate Resume
                                </button>

                                <Link
                                    to={`/jobs/${job.id}/edit`}
                                    className="bg-white border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-100 text-center"
                                >
                                    Edit Job
                                </Link>

                                <button
                                    onClick={() => handleDeleteJob(job.id)}
                                    className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                                >
                                    Delete Job
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </AppLayout>
    );
}