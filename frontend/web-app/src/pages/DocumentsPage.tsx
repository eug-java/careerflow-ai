import { useEffect, useState } from "react";
import AppLayout from "../layouts/AppLayout";

import {
    fetchDocuments,
    fetchDocumentContent,
    downloadDocumentPdf,
    deleteDocument,
    downloadDocumentDocx,
} from "../api/documentApi";
import type { GeneratedDocument } from "../api/documentApi";

import { fetchProfiles } from "../api/profileApi";
import type { Profile } from "../api/profileApi";

import { fetchJobs } from "../api/jobApi";
import type { Job } from "../api/jobApi";

export default function DocumentsPage() {
    const [documents, setDocuments] = useState<GeneratedDocument[]>([]);
    const [profiles, setProfiles] = useState<Profile[]>([]);
    const [jobs, setJobs] = useState<Job[]>([]);

    const [selectedDocumentId, setSelectedDocumentId] = useState<string | null>(
        null
    );
    const [content, setContent] = useState<string>("");

    useEffect(() => {
        fetchDocuments().then(setDocuments);
        fetchProfiles().then(setProfiles);
        fetchJobs().then(setJobs);
    }, []);

    function getProfileLabel(profileId: string) {
        const profile = profiles.find((item) => item.id === profileId);

        return profile
            ? `${profile.fullName} - ${profile.professionalTitle}`
            : profileId;
    }

    function getJobLabel(jobId: string) {
        const job = jobs.find((item) => item.id === jobId);

        return job ? `${job.title} at ${job.companyName}` : jobId;
    }

    async function handlePreview(documentId: string) {
        const documentContent = await fetchDocumentContent(documentId);

        setSelectedDocumentId(documentId);
        setContent(documentContent);
    }

    async function handleDelete(documentId: string) {
        const confirmed = window.confirm("Delete this document?");

        if (!confirmed) {
            return;
        }

        await deleteDocument(documentId);

        setDocuments((current) =>
            current.filter((doc) => doc.id !== documentId)
        );

        if (selectedDocumentId === documentId) {
            setSelectedDocumentId(null);
            setContent("");
        }
    }

    return (
        <AppLayout>
            <h1 className="text-4xl font-bold mb-8">Documents</h1>

            <p className="mb-4 text-slate-500">
                Documents loaded: {documents.length}
            </p>

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
                <div className="grid gap-6">
                    {documents.map((doc) => (
                        <div key={doc.id} className="bg-white rounded-2xl shadow p-6">
                            <h2 className="text-xl font-semibold">{doc.documentType}</h2>

                            <p className="text-slate-500 mt-1">{doc.fileName}</p>

                            <div className="mt-4 text-sm text-slate-600 break-all">
                                <p>Profile: {getProfileLabel(doc.profileId)}</p>
                                <p>Job: {getJobLabel(doc.jobId)}</p>
                                <p>Bucket: {doc.storageBucket}</p>
                                <p>Key: {doc.storageKey}</p>
                                <p>Created: {doc.createdAt}</p>
                            </div>

                            <div className="flex gap-3 mt-5">
                                <button
                                    onClick={() => handlePreview(doc.id)}
                                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                                >
                                    Preview
                                </button>

                                <button
                                    onClick={() => downloadDocumentPdf(doc.id, doc.fileName)}
                                    className="bg-white border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-100"
                                >
                                    Download PDF
                                </button>

                                <button
                                    onClick={() => downloadDocumentDocx(doc.id, doc.fileName)}
                                    className="bg-white border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-100"
                                >
                                    Download DOCX
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

                    {!selectedDocumentId && (
                        <p className="text-slate-500">
                            Select a document to preview.
                        </p>
                    )}

                    {selectedDocumentId && (
                        <pre className="whitespace-pre-wrap text-sm text-slate-800 bg-slate-50 rounded-xl p-4 overflow-auto max-h-[700px]">
              {content}
            </pre>
                    )}
                </div>
            </div>
        </AppLayout>
    );
}