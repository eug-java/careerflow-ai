import { Link } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { Badge, EmptyState, LoadingGrid, QueryErrorState } from "../components/ui/Card";
import { useDashboardQueries } from "../hooks/useDashboardQueries";
import { computeSkillGap } from "../lib/dashboardUtils";

function aggregateMissingSkills(
    items: Array<{ jobTitle: string; company: string; missing: string[] }>
): Array<{ skill: string; count: number }> {
    const counts = new Map<string, number>();
    for (const item of items) {
        for (const skill of item.missing) {
            const key = skill.toLowerCase();
            counts.set(key, (counts.get(key) ?? 0) + 1);
        }
    }
    return [...counts.entries()]
        .map(([skill, count]) => ({ skill, count }))
        .sort((a, b) => b.count - a.count);
}

export default function SkillGapInsightsPage() {
    const { profiles, jobs, matches, isLoading, isError, refetchAll } = useDashboardQueries();

    const insights = matches
        .map((match) => {
            const profile = profiles.find((item) => item.id === match.profileId);
            const job = jobs.find((item) => item.id === match.jobId);
            const gap = computeSkillGap(profile, job);
            return {
                matchId: match.id,
                profileId: match.profileId,
                jobId: match.jobId,
                profileName: profile?.fullName ?? "Unknown profile",
                jobTitle: job?.title ?? "Unknown job",
                company: job?.companyName ?? "",
                score: Number(match.totalScore),
                gap,
            };
        })
        .filter((item) => item.gap.missing.length > 0)
        .sort((a, b) => b.gap.missing.length - a.gap.missing.length);

    const topMissing = aggregateMissingSkills(
        insights.map((item) => ({
            jobTitle: item.jobTitle,
            company: item.company,
            missing: item.gap.missing,
        }))
    ).slice(0, 10);

    return (
        <AppLayout>
            <div className="mb-8">
                <h1 className="text-4xl font-bold">Skill gap insights</h1>
                <p className="text-slate-500 mt-2">
                    Required skills you are missing across your saved matches.
                </p>
            </div>

            {isLoading ? (
                <LoadingGrid count={3} />
            ) : isError ? (
                <QueryErrorState onRetry={() => void refetchAll()} />
            ) : insights.length === 0 ? (
                <EmptyState
                    title="No skill gaps detected"
                    description="Calculate matches between your profiles and jobs to see which skills to develop."
                    actionLabel="Go to jobs"
                    actionHref="/jobs"
                />
            ) : (
                <>
                    <div className="bg-white rounded-2xl shadow p-6 mb-8">
                        <h2 className="text-xl font-semibold mb-4">Most common missing skills</h2>
                        <div className="flex flex-wrap gap-2">
                            {topMissing.map((item) => (
                                <Badge key={item.skill} tone="warning">
                                    {item.skill} · {item.count} job{item.count === 1 ? "" : "s"}
                                </Badge>
                            ))}
                        </div>
                    </div>

                    <div className="grid gap-4">
                        {insights.map((item) => (
                            <div key={item.matchId} className="bg-white rounded-2xl shadow p-6">
                                <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4">
                                    <div>
                                        <div className="flex items-center gap-3 flex-wrap">
                                            <h2 className="text-lg font-semibold">{item.jobTitle}</h2>
                                            <Badge tone={item.score >= 70 ? "success" : "warning"}>
                                                {item.score.toFixed(0)}% match
                                            </Badge>
                                        </div>
                                        <p className="text-slate-500 mt-1">
                                            {item.company} · {item.profileName}
                                        </p>
                                        <div className="mt-4">
                                            <p className="text-sm font-medium text-rose-700 mb-2">
                                                Missing required skills ({item.gap.missing.length})
                                            </p>
                                            <p className="text-slate-700">{item.gap.missing.join(", ")}</p>
                                        </div>
                                        {item.gap.matched.length > 0 && (
                                            <div className="mt-3">
                                                <p className="text-sm font-medium text-emerald-700 mb-2">
                                                    Already matched
                                                </p>
                                                <p className="text-slate-600">{item.gap.matched.join(", ")}</p>
                                            </div>
                                        )}
                                    </div>
                                    <div className="flex flex-col gap-2 min-w-40">
                                        <Link
                                            to={`/matches/${item.matchId}`}
                                            className="text-center bg-indigo-600 text-white px-4 py-2 rounded-xl hover:bg-indigo-500"
                                        >
                                            Match details
                                        </Link>
                                        <Link
                                            to={`/profiles/${item.profileId}`}
                                            className="text-center border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-50"
                                        >
                                            Update profile
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            )}
        </AppLayout>
    );
}
