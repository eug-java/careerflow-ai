import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { CATEGORY_LABELS, fetchEmailSummary } from "../../api/emailApi";
import { Card } from "../ui/Card";

export function EmailSnapshot() {
    const summaryQuery = useQuery({
        queryKey: ["email-summary"],
        queryFn: fetchEmailSummary,
    });

    const summary = summaryQuery.data;

    return (
        <Card className="mt-8">
            <div className="flex items-center justify-between mb-4">
                <h2 className="text-xl font-semibold">Recruiter email</h2>
                <Link to="/email" className="text-sm text-indigo-600 hover:underline">
                    Open inbox
                </Link>
            </div>

            {!summary?.accountConfigured ? (
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
                    <p className="text-slate-500 text-sm">
                        Connect your mailbox to classify offers, rejections, and vacancy emails.
                    </p>
                    <Link
                        to="/email/settings"
                        className="inline-flex justify-center bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                    >
                        Connect email
                    </Link>
                </div>
            ) : (
                <div className="grid md:grid-cols-5 gap-3">
                    <div className="border border-slate-100 rounded-xl p-3">
                        <p className="text-sm text-slate-500">Total</p>
                        <p className="text-2xl font-bold">{summary.totalMessages}</p>
                    </div>
                    {(["OFFER", "REJECTION", "VACANCY", "REVISION_REQUEST"] as const).map(
                        (category) => (
                            <div key={category} className="border border-slate-100 rounded-xl p-3">
                                <p className="text-sm text-slate-500">{CATEGORY_LABELS[category]}</p>
                                <p className="text-2xl font-bold">
                                    {summary.byCategory?.[category] ?? 0}
                                </p>
                            </div>
                        )
                    )}
                </div>
            )}
        </Card>
    );
}
