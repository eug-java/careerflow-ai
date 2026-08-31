import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import { fetchMatchById } from "../api/matchingApi";
import { useJobsQuery, useProfilesQuery } from "../hooks/useDashboardQueries";
import { Badge, LoadingGrid, QueryErrorState, ScoreBar } from "../components/ui/Card";
import { formatRelativeTime } from "../lib/dashboardUtils";

export default function MatchDetailPage() {
    const { id: matchId = "" } = useParams();
    const { data: profiles = [] } = useProfilesQuery();
    const { data: jobs = [] } = useJobsQuery();

    const matchQuery = useQuery({
        queryKey: ["match", matchId],
        queryFn: () => fetchMatchById(matchId),
        enabled: Boolean(matchId),
    });

    if (matchQuery.isLoading) {
        return (
            <AppLayout>
                <LoadingGrid count={2} />
            </AppLayout>
        );
    }

    if (matchQuery.isError || !matchQuery.data) {
        return (
            <AppLayout>
                <QueryErrorState
                    title="Match not found"
                    description="This match may have been removed or you may not have access."
                    onRetry={() => void matchQuery.refetch()}
                />
            </AppLayout>
        );
    }

    const match = matchQuery.data;
    const job = jobs.find((item) => item.id === match.jobId);
    const profile = profiles.find((item) => item.id === match.profileId);
    const score = Number(match.totalScore);

    return (
        <AppLayout>
            <div className="mb-8">
                <Link to="/matches" className="text-sm text-indigo-600 hover:underline">
                    ← Back to match history
                </Link>
                <div className="flex items-center gap-3 mt-4">
                    <h1 className="text-4xl font-bold">{job?.title ?? "Match details"}</h1>
                    <Badge
                        tone={score >= 70 ? "success" : score >= 40 ? "warning" : "danger"}
                    >
                        {score.toFixed(0)}% match
                    </Badge>
                </div>
                <p className="text-slate-500 mt-2">
                    {job?.companyName} · {profile?.fullName} · {formatRelativeTime(match.createdAt)}
                </p>
            </div>

            <div className="bg-white rounded-2xl shadow p-6 mb-6">
                <h2 className="text-xl font-semibold mb-4">Explanation</h2>
                <p className="text-slate-700">{match.explanation}</p>
            </div>

            <div className="grid md:grid-cols-2 gap-4 mb-8">
                <ScoreBar label="Skills" score={Number(match.skillsScore)} />
                <ScoreBar label="Location" score={Number(match.locationScore)} />
                <ScoreBar label="Experience" score={Number(match.experienceScore ?? 0)} />
                <ScoreBar label="Salary" score={Number(match.salaryScore)} />
            </div>

            <div className="flex gap-3">
                <Link
                    to={`/jobs/${match.jobId}`}
                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                >
                    Open job
                </Link>
                <Link
                    to={`/profiles/${match.profileId}`}
                    className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                >
                    Open profile
                </Link>
            </div>
        </AppLayout>
    );
}
