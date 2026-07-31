import { Link, useParams } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import { fetchProfileById } from "../api/profileApi";
import { useDocumentsQuery, useJobsQuery, useMatchesQuery } from "../hooks/useDashboardQueries";
import { Badge, Card, EmptyState, LoadingGrid, ScoreBar } from "../components/ui/Card";
import { calculateProfileReadiness, computeSkillGap, getTopMatches } from "../lib/dashboardUtils";

export default function ProfileDetailPage() {
    const { id = "" } = useParams();

    const profileQuery = useQuery({
        queryKey: ["profile", id],
        queryFn: () => fetchProfileById(id),
        enabled: Boolean(id),
    });

    const jobsQuery = useJobsQuery();
    const matchesQuery = useMatchesQuery(id);
    const documentsQuery = useDocumentsQuery(id);

    if (profileQuery.isLoading) {
        return (
            <AppLayout>
                <LoadingGrid count={2} />
            </AppLayout>
        );
    }

    if (profileQuery.isError || !profileQuery.data) {
        return (
            <AppLayout>
                <EmptyState
                    title="Profile not found"
                    description="This profile may have been deleted."
                    actionLabel="Back to profiles"
                    actionHref="/profiles"
                />
            </AppLayout>
        );
    }

    const profile = profileQuery.data;
    const jobs = jobsQuery.data ?? [];
    const matches = matchesQuery.data ?? [];
    const documents = documentsQuery.data ?? [];
    const readiness = calculateProfileReadiness(profile);
    const topMatches = getTopMatches(matches, jobs, [profile], 3);

    return (
        <AppLayout>
            <div className="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 mb-8">
                <div>
                    <Link to="/profiles" className="text-sm text-indigo-600 hover:underline">
                        ← Back to profiles
                    </Link>
                    <h1 className="text-4xl font-bold mt-2">{profile.fullName}</h1>
                    <p className="text-slate-500 text-lg">{profile.professionalTitle}</p>
                    <p className="text-slate-500 mt-1">
                        {profile.email} · {profile.location}
                    </p>
                </div>
                <Link
                    to={`/profiles/${profile.id}/edit`}
                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700 h-fit"
                >
                    Edit profile
                </Link>
            </div>

            <div className="grid xl:grid-cols-3 gap-6 mb-8">
                <Card>
                    <h2 className="font-semibold mb-2">Readiness</h2>
                    <p className="text-3xl font-bold text-indigo-600">{readiness.score}%</p>
                    <div className="h-2 bg-slate-100 rounded-full mt-3 overflow-hidden">
                        <div
                            className="h-full bg-indigo-600 rounded-full"
                            style={{ width: `${readiness.score}%` }}
                        />
                    </div>
                </Card>
                <Card>
                    <h2 className="font-semibold mb-2">Matches</h2>
                    <p className="text-3xl font-bold">{matches.length}</p>
                </Card>
                <Card>
                    <h2 className="font-semibold mb-2">Documents</h2>
                    <p className="text-3xl font-bold">{documents.length}</p>
                </Card>
            </div>

            <div className="grid xl:grid-cols-2 gap-6 mb-8">
                <Card>
                    <h2 className="text-xl font-semibold mb-4">Summary</h2>
                    <p className="text-slate-700 whitespace-pre-wrap">{profile.summary}</p>
                </Card>

                <Card>
                    <h2 className="text-xl font-semibold mb-4">Skills</h2>
                    {(profile.skills ?? []).length === 0 ? (
                        <p className="text-slate-500">No skills added yet.</p>
                    ) : (
                        <div className="flex flex-wrap gap-2">
                            {(profile.skills ?? []).map((skill) => (
                                <Badge key={skill.id ?? skill.name}>{skill.name}</Badge>
                            ))}
                        </div>
                    )}
                </Card>
            </div>

            <Card className="mb-8">
                <h2 className="text-xl font-semibold mb-4">Experience</h2>
                {(profile.experiences ?? []).length === 0 ? (
                    <p className="text-slate-500">No experience entries yet.</p>
                ) : (
                    <div className="space-y-4">
                        {(profile.experiences ?? []).map((exp) => (
                            <div key={exp.id ?? `${exp.companyName}-${exp.positionTitle}`} className="border-b border-slate-100 pb-4 last:border-0">
                                <p className="font-medium">{exp.positionTitle}</p>
                                <p className="text-slate-500">{exp.companyName}</p>
                                <p className="text-sm text-slate-400 mt-1">{exp.description}</p>
                            </div>
                        ))}
                    </div>
                )}
            </Card>

            <Card className="mb-8">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-xl font-semibold">Top matches for this profile</h2>
                    <Link to="/matches" className="text-sm text-indigo-600 hover:underline">
                        View all
                    </Link>
                </div>
                {topMatches.length === 0 ? (
                    <p className="text-slate-500">No matches calculated for this profile yet.</p>
                ) : (
                    <div className="space-y-4">
                        {topMatches.map(({ match, job }) => {
                            const gap = computeSkillGap(profile, job);
                            return (
                                <div key={match.id} className="border border-slate-100 rounded-xl p-4">
                                    <div className="flex justify-between items-center">
                                        <div>
                                            <p className="font-semibold">{job?.title}</p>
                                            <p className="text-sm text-slate-500">{job?.companyName}</p>
                                        </div>
                                        <Link
                                            to={`/jobs/${match.jobId}`}
                                            className="text-indigo-600 font-semibold"
                                        >
                                            {Number(match.totalScore).toFixed(0)}%
                                        </Link>
                                    </div>
                                    {job && (
                                        <div className="mt-3 grid md:grid-cols-2 gap-3 text-sm">
                                            <div>
                                                <p className="font-medium text-emerald-700">Matched skills</p>
                                                <p className="text-slate-600">
                                                    {gap.matched.length > 0
                                                        ? gap.matched.join(", ")
                                                        : "None"}
                                                </p>
                                            </div>
                                            <div>
                                                <p className="font-medium text-rose-700">Missing skills</p>
                                                <p className="text-slate-600">
                                                    {gap.missing.length > 0
                                                        ? gap.missing.join(", ")
                                                        : "None"}
                                                </p>
                                            </div>
                                        </div>
                                    )}
                                    <div className="grid md:grid-cols-3 gap-3 mt-4">
                                        <ScoreBar label="Skills" score={Number(match.skillsScore)} />
                                        <ScoreBar label="Location" score={Number(match.locationScore)} />
                                        <ScoreBar label="Salary" score={Number(match.salaryScore)} />
                                    </div>
                                </div>
                            );
                        })}
                    </div>
                )}
            </Card>
        </AppLayout>
    );
}
