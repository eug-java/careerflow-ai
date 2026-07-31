import { Link } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { Badge, EmptyState, LoadingGrid, ScoreBar } from "../components/ui/Card";
import { useDashboardQueries } from "../hooks/useDashboardQueries";
import { formatRelativeTime } from "../lib/dashboardUtils";

export default function MatchHistoryPage() {
    const { profiles, jobs, matches, isLoading } = useDashboardQueries();

    const sortedMatches = [...matches].sort(
        (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );

    return (
        <AppLayout>
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-4xl font-bold">Match history</h1>
                    <p className="text-slate-500 mt-2">
                        All profile–job match calculations with score breakdown.
                    </p>
                </div>
                <Link
                    to="/jobs"
                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                >
                    Calculate new match
                </Link>
            </div>

            {isLoading ? (
                <LoadingGrid count={3} />
            ) : sortedMatches.length === 0 ? (
                <EmptyState
                    title="No matches yet"
                    description="Go to Jobs, select a profile, and calculate a match to build your history."
                    actionLabel="Go to jobs"
                    actionHref="/jobs"
                />
            ) : (
                <div className="grid gap-4">
                    {sortedMatches.map((match) => {
                        const job = jobs.find((item) => item.id === match.jobId);
                        const profile = profiles.find((item) => item.id === match.profileId);
                        const score = Number(match.totalScore);

                        return (
                            <div key={match.id} className="bg-white rounded-2xl shadow p-6">
                                <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                                    <div>
                                        <div className="flex items-center gap-3">
                                            <h2 className="text-xl font-semibold">
                                                {job?.title ?? "Unknown job"}
                                            </h2>
                                            <Badge
                                                tone={
                                                    score >= 70
                                                        ? "success"
                                                        : score >= 40
                                                          ? "warning"
                                                          : "danger"
                                                }
                                            >
                                                {score.toFixed(0)}% match
                                            </Badge>
                                        </div>
                                        <p className="text-slate-500 mt-1">
                                            {job?.companyName} · {profile?.fullName}
                                        </p>
                                        <p className="text-sm text-slate-400 mt-2">
                                            {formatRelativeTime(match.createdAt)}
                                        </p>
                                        <p className="mt-4 text-slate-700">{match.explanation}</p>
                                    </div>
                                    <div className="flex flex-col gap-2 min-w-40">
                                        <Link
                                            to={`/jobs/${match.jobId}`}
                                            className="text-center bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                                        >
                                            Open job
                                        </Link>
                                        <Link
                                            to={`/profiles/${match.profileId}`}
                                            className="text-center border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                        >
                                            Open profile
                                        </Link>
                                    </div>
                                </div>
                                <div className="grid md:grid-cols-3 gap-4 mt-6">
                                    <ScoreBar label="Skills" score={Number(match.skillsScore)} />
                                    <ScoreBar
                                        label="Location"
                                        score={Number(match.locationScore)}
                                    />
                                    <ScoreBar label="Salary" score={Number(match.salaryScore)} />
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </AppLayout>
    );
}
