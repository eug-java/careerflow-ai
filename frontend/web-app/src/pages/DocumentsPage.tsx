import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import {
    fetchDocumentContent,
    downloadDocumentPdf,
    deleteDocument,
    downloadDocumentDocx,
} from "../api/documentApi";
import { useDocumentsQuery, useJobsQuery, useProfilesQuery } from "../hooks/useDashboardQueries";
import { Badge, EmptyState, LoadingGrid } from "../components/ui/Card";
import { formatRelativeTime } from "../lib/dashboardUtils";
import { useToast } from "../hooks/useToast";

export default function DocumentsPage() {
    const [profileFilter, setProfileFilter] = useState("");
    const [jobFilter, setJobFilter] = useState("");
    const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(null);
    const [content, setContent] = useState<string>("");
    const { pushToast } = useToast();
    const queryClient = useQueryClient();

    const { data: profiles = [] } = useProfilesQuery();
    const { data: jobs = [] } = useJobsQuery();
    const { data: documents = [], isLoading } = useDocumentsQuery(
        profileFilter || undefined,
        jobFilter || undefined
    );

    function getProfileLabel(profileId: string) {
        const profile = profiles.find((item) => item.id === profileId);
        return profile ? `${profile.fullName}` : profileId;
    }

    function getJobLabel(jobId: string) {
        const job = jobs.find((item) => item.id === jobId);
        return job ? `${job.title} at ${job.companyName}` : jobId;
    }

    async function handlePreview(documentId: string) {
        try {
            const documentContent = await fetchDocumentContent(documentId);
            setSelectedDocumentId(documentId);
            setContent(documentContent);
        } catch (error) {
            pushToast("error", error instanceof Error ? error.message : "Preview failed");
        }
    }

    async function handleDelete(documentId: string) {
        const confirmed = window.confirm("Delete this document?");
        if (!confirmed) {
            return;
        }
        await deleteDocument(documentId);
        await queryClient.invalidateQueries({ queryKey: ["documents"] });
        if (selectedDocumentId === documentId) {
            setSelectedDocumentId(null);
            setContent("");
        }
        pushToast("success", "Document deleted.");
    }

    return (
        <AppLayout>
            <div className="mb-8">
                <h1 className="text-4xl font-bold">Documents</h1>
                <p className="text-slate-500 mt-2">Preview and download AI-generated resumes and cover letters.</p>
            </div>

            <div className="grid md:grid-cols-2 gap-4 mb-6">
                <select
                    value={profileFilter}
                    onChange={(event) => setProfileFilter(event.target.value)}
                    className="border border-slate-300 rounded-xl px-4 py-2 bg-white"
                >
                    <option value="">All profiles</option>
                    {profiles.map((profile) => (
                        <option key={profile.id} value={profile.id}>
                            {profile.fullName}
                        </option>
                    ))}
                </select>
                <select
                    value={jobFilter}
                    onChange={(event) => setJobFilter(event.target.value)}
                    className="border border-slate-300 rounded-xl px-4 py-2 bg-white"
                >
                    <option value="">All jobs</option>
                    {jobs.map((job) => (
                        <option key={job.id} value={job.id}>
                            {job.title} — {job.companyName}
                        </option>
                    ))}
                </select>
            </div>

            {isLoading ? (
                <LoadingGrid count={2} />
            ) : documents.length === 0 ? (
                <EmptyState
                    title="No documents yet"
                    description="Generate a cover letter or resume from Jobs or the dashboard quick actions."
                    actionLabel="Go to jobs"
                    actionHref="/jobs"
                />
            ) : (
                <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                    <div className="grid gap-4">
                        {documents.map((doc) => (
                            <div key={doc.id} className="bg-white rounded-2xl shadow p-6">
                                <div className="flex items-center justify-between gap-2">
                                    <Badge tone={doc.documentType === "RESUME" ? "info" : "success"}>
                                        {doc.documentType.replace("_", " ")}
                                    </Badge>
                                    <span className="text-xs text-slate-400">
                                        {formatRelativeTime(doc.createdAt)}
                                    </span>
                                </div>
                                <h2 className="text-xl font-semibold mt-3">{doc.fileName}</h2>
                                <div className="mt-3 text-sm text-slate-600">
                                    <p>Profile: {getProfileLabel(doc.profileId)}</p>
                                    <p>Job: {getJobLabel(doc.jobId)}</p>
                                </div>
                                <div className="flex flex-wrap gap-2 mt-5">
                                    <button
                                        onClick={() => handlePreview(doc.id)}
                                        className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                                    >
                                        Preview
                                    </button>
                                    <button
                                        onClick={() => downloadDocumentPdf(doc.id, doc.fileName)}
                                        className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                    >
                                        PDF
                                    </button>
                                    <button
                                        onClick={() => downloadDocumentDocx(doc.id, doc.fileName)}
                                        className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                    >
                                        DOCX
                                    </button>
                                    <button
                                        onClick={() => handleDelete(doc.id)}
                                        className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="bg-white rounded-2xl shadow p-6 sticky top-8 h-fit">
                        <h2 className="text-2xl font-semibold mb-4">Preview</h2>
                        {!selectedDocumentId ? (
                            <p className="text-slate-500">Select a document to preview.</p>
                        ) : (
                            <pre className="whitespace-pre-wrap text-sm text-slate-800 bg-slate-50 rounded-xl p-4 overflow-auto max-h-[700px]">
                                {content}
                            </pre>
                        )}
                    </div>
                </div>
            )}
        </AppLayout>
    );
}
