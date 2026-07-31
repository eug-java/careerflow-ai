import { Link } from "react-router-dom";
import type { GeneratedDocument } from "../../api/documentApi";
import type { Job } from "../../api/jobApi";
import type { Profile } from "../../api/profileApi";
import { Badge, Card } from "../ui/Card";
import { formatRelativeTime } from "../../lib/dashboardUtils";

interface Props {
    documents: GeneratedDocument[];
    profiles: Profile[];
    jobs: Job[];
}

export function DocumentsSnapshot({ documents, profiles, jobs }: Props) {
    return (
        <Card>
            <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold">Recent documents</h2>
                <Link to="/documents" className="text-sm text-indigo-600 hover:underline">
                    View library
                </Link>
            </div>

            {documents.length === 0 ? (
                <p className="text-slate-500 text-sm">Generated documents will appear here.</p>
            ) : (
                <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-4">
                    {documents.map((doc) => {
                        const profile = profiles.find((item) => item.id === doc.profileId);
                        const job = jobs.find((item) => item.id === doc.jobId);

                        return (
                            <div
                                key={doc.id}
                                className="border border-slate-100 rounded-xl p-4"
                            >
                                <div className="flex items-center justify-between gap-2">
                                    <Badge tone={doc.documentType === "RESUME" ? "info" : "success"}>
                                        {doc.documentType.replace("_", " ")}
                                    </Badge>
                                    <span className="text-xs text-slate-400">
                                        {formatRelativeTime(doc.createdAt)}
                                    </span>
                                </div>
                                <p className="font-medium mt-3">{job?.title ?? doc.fileName}</p>
                                <p className="text-sm text-slate-500">
                                    {profile?.fullName} · {job?.companyName}
                                </p>
                            </div>
                        );
                    })}
                </div>
            )}
        </Card>
    );
}
