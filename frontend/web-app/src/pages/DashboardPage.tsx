import { Link } from "react-router-dom";
import {
    Badge,
    Card,
    EmptyState,
    KpiCard,
    LoadingGrid,
    ScoreBar,
} from "../components/ui/Card";
import { useDashboardQueries } from "../hooks/useDashboardQueries";
import {
    buildActivityFeed,
    buildDashboardSummary,
    calculateProfileReadiness,
    formatRelativeTime,
    getTopMatches,
    greetingName,
    resolveNextBestAction,
} from "../lib/dashboardUtils";
import AppLayout from "../layouts/AppLayout";
import { QuickActionsPanel } from "../components/dashboard/QuickActionsPanel";
import { DocumentsSnapshot } from "../components/dashboard/DocumentsSnapshot";
import { MatchDistributionChart } from "../components/dashboard/MatchDistributionChart";
import { EmailSnapshot } from "../components/dashboard/EmailSnapshot";
import { AiOnboardingBanner } from "../components/dashboard/AiOnboardingBanner";
import { OnboardingWizard } from "../components/dashboard/OnboardingWizard";

export default function DashboardPage() {
    const { profiles, jobs, matches, documents, workflows, isLoading, isError } =
        useDashboardQueries();

    if (isLoading) {
        return (
            <AppLayout>
                <LoadingGrid count={6} />
            </AppLayout>
        );
    }

    if (isError) {
        return (
            <AppLayout>
                <EmptyState
                    title="Unable to load dashboard"
                    description="Check that backend services are running and try refreshing the page."
                />
            </AppLayout>
        );
    }

    const summary = buildDashboardSummary(profiles, jobs, matches, documents, workflows);
    const primaryProfile = profiles[0];
    const readiness = calculateProfileReadiness(primaryProfile);
    const topMatches = getTopMatches(matches, jobs, profiles);
    const activity = buildActivityFeed(matches, documents, workflows, jobs, profiles).slice(
        0,
        8
    );
    const nextAction = resolveNextBestAction(profiles, jobs, matches, documents);
    const runningWorkflows = workflows.filter((item) => item.status === "RUNNING");

    return (
        <AppLayout>
            <OnboardingWizard />
            <AiOnboardingBanner />
            <div className="mb-8 flex flex-col lg:flex-row lg:items-end lg:justify-between gap-4">
                <div>
                    <p className="text-slate-500">{greetingName()} 👋</p>
                    <h1 className="text-4xl font-bold mt-1">Career Command Center</h1>
                    <p className="text-slate-500 mt-2">
                        Track matches, documents, and active generation workflows in one place.
                    </p>
                </div>
                {primaryProfile && (
                    <Card className="lg:w-80">
                        <div className="flex justify-between items-center mb-2">
                            <span className="text-sm text-slate-500">Profile readiness</span>
                            <span className="font-semibold">{readiness.score}%</span>
                        </div>
                        <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                            <div
                                className="h-full bg-indigo-600 rounded-full"
                                style={{ width: `${readiness.score}%` }}
                            />
                        </div>
                        <Link
                            to={`/profiles/${primaryProfile.id}`}
                            className="text-sm text-indigo-600 mt-3 inline-block hover:underline"
                        >
                            View {primaryProfile.fullName}
                        </Link>
                    </Card>
                )}
            </div>

            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-6 mb-8">
                <KpiCard label="Profiles" value={summary.profilesCount} href="/profiles" />
                <KpiCard label="Jobs" value={summary.jobsCount} href="/jobs" />
                <KpiCard
                    label="Matches"
                    value={summary.matchesCount}
                    href="/matches"
                    accent="text-indigo-600"
                />
                <KpiCard label="Documents" value={summary.documentsCount} href="/documents" />
                <KpiCard
                    label="Avg match"
                    value={`${summary.avgMatchScore}%`}
                    hint="Across all calculations"
                    accent="text-emerald-600"
                />
                <KpiCard
                    label="Running"
                    value={summary.runningWorkflowsCount}
                    hint="Active workflows"
                    accent="text-amber-600"
                />
            </div>

            <Card className="mb-8 bg-gradient-to-r from-indigo-600 to-violet-600 text-white border-0">
                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                    <div>
                        <p className="text-indigo-100 text-sm uppercase tracking-wide">
                            Next best action
                        </p>
                        <h2 className="text-2xl font-semibold mt-1">{nextAction.title}</h2>
                        <p className="text-indigo-100 mt-2 max-w-2xl">{nextAction.description}</p>
                    </div>
                    <Link
                        to={nextAction.href}
                        className="inline-flex justify-center bg-white text-indigo-700 px-5 py-3 rounded-xl font-medium hover:bg-indigo-50"
                    >
                        {nextAction.buttonLabel}
                    </Link>
                </div>
            </Card>

            <div className="grid xl:grid-cols-3 gap-6 mb-8">
                <Card className="xl:col-span-2">
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-xl font-semibold">Top matches</h2>
                        <Link to="/matches" className="text-sm text-indigo-600 hover:underline">
                            View all
                        </Link>
                    </div>
                    {topMatches.length === 0 ? (
                        <p className="text-slate-500">No matches yet. Calculate your first match from Jobs.</p>
                    ) : (
                        <div className="space-y-5">
                            {topMatches.map(({ match, job, profile }) => (
                                <div
                                    key={match.id}
                                    className="border border-slate-100 rounded-xl p-4"
                                >
                                    <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                                        <div>
                                            <p className="font-semibold text-lg">
                                                {job?.title ?? "Unknown job"}
                                            </p>
                                            <p className="text-slate-500 text-sm">
                                                {job?.companyName} · {profile?.fullName}
                                            </p>
                                        </div>
                                        <div className="flex items-center gap-3">
                                            <span className="text-2xl font-bold text-indigo-600">
                                                {Number(match.totalScore).toFixed(0)}%
                                            </span>
                                            <Link
                                                to={`/jobs/${match.jobId}`}
                                                className="text-sm bg-slate-900 text-white px-3 py-2 rounded-lg hover:bg-slate-700"
                                            >
                                                Open
                                            </Link>
                                        </div>
                                    </div>
                                    <div className="grid md:grid-cols-3 gap-3 mt-4">
                                        <ScoreBar label="Skills" score={Number(match.skillsScore)} />
                                        <ScoreBar
                                            label="Location"
                                            score={Number(match.locationScore)}
                                        />
                                        <ScoreBar label="Experience" score={Number(match.experienceScore ?? 0)} />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </Card>

                <div className="space-y-6">
                    <Card>
                        <div className="flex items-center justify-between mb-4">
                            <h2 className="text-xl font-semibold">Active pipeline</h2>
                            <Link to="/workflows" className="text-sm text-indigo-600 hover:underline">
                                View all
                            </Link>
                        </div>
                        {runningWorkflows.length === 0 ? (
                            <p className="text-slate-500 text-sm">No workflows running right now.</p>
                        ) : (
                            <div className="space-y-3">
                                {runningWorkflows.map((workflow) => (
                                    <div
                                        key={workflow.processInstanceKey}
                                        className="border border-slate-100 rounded-xl p-3"
                                    >
                                        <div className="flex items-center justify-between">
                                            <Badge tone="warning">{workflow.status}</Badge>
                                            <span className="text-xs text-slate-400">
                                                #{workflow.processInstanceKey}
                                            </span>
                                        </div>
                                        <p className="text-sm mt-2">{workflow.message}</p>
                                    </div>
                                ))}
                            </div>
                        )}
                    </Card>

                    <MatchDistributionChart matches={matches} />
                </div>
            </div>

            <div className="grid xl:grid-cols-3 gap-6 mb-8">
                <Card className="xl:col-span-2">
                    <h2 className="text-xl font-semibold mb-4">Recent activity</h2>
                    {activity.length === 0 ? (
                        <p className="text-slate-500">Activity will appear after matches and documents are created.</p>
                    ) : (
                        <div className="space-y-3">
                            {activity.map((item) => (
                                <div
                                    key={item.id}
                                    className="flex items-start justify-between gap-4 border-b border-slate-100 pb-3 last:border-0"
                                >
                                    <div>
                                        <p className="font-medium">{item.title}</p>
                                        <p className="text-sm text-slate-500">{item.subtitle}</p>
                                    </div>
                                    <span className="text-xs text-slate-400 whitespace-nowrap">
                                        {formatRelativeTime(item.timestamp)}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </Card>

                <QuickActionsPanel
                    profiles={profiles}
                    jobs={jobs}
                />
            </div>

            <DocumentsSnapshot
                documents={documents.slice(0, 5)}
                profiles={profiles}
                jobs={jobs}
            />

            <EmailSnapshot />

            {primaryProfile && readiness.checks.some((check) => !check.done) && (
                <Card className="mt-8">
                    <h2 className="text-xl font-semibold mb-4">Profile checklist</h2>
                    <div className="grid md:grid-cols-2 gap-3">
                        {readiness.checks.map((check) => (
                            <div
                                key={check.label}
                                className="flex items-center justify-between border border-slate-100 rounded-xl px-4 py-3"
                            >
                                <span className={check.done ? "text-slate-500 line-through" : ""}>
                                    {check.label}
                                </span>
                                {!check.done && check.href && (
                                    <Link to={check.href} className="text-sm text-indigo-600">
                                        Fix
                                    </Link>
                                )}
                            </div>
                        ))}
                    </div>
                </Card>
            )}
        </AppLayout>
    );
}
