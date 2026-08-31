import { Link } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { deleteProfile } from "../api/profileApi";
import { useProfilesQuery } from "../hooks/useDashboardQueries";
import { calculateProfileReadiness } from "../lib/dashboardUtils";
import { Badge, EmptyState, LoadingGrid, QueryErrorState } from "../components/ui/Card";
import { useQueryClient } from "@tanstack/react-query";

export default function ProfilesPage() {
    const { data: profiles = [], isLoading, isError, refetch } = useProfilesQuery();
    const queryClient = useQueryClient();

    async function handleDelete(profileId: string) {
        const confirmed = window.confirm("Delete this profile?");

        if (!confirmed) {
            return;
        }

        await deleteProfile(profileId);
        await queryClient.invalidateQueries({ queryKey: ["profiles"] });
    }

    return (
        <AppLayout>
            <div className="flex items-center justify-between mb-8">
                <div>
                    <h1 className="text-4xl font-bold">Profiles</h1>
                    <p className="text-slate-500 mt-2">Manage candidate profiles for matching and document generation.</p>
                </div>

                <Link
                    to="/profiles/new"
                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                >
                    Add Profile
                </Link>
            </div>

            {isLoading ? (
                <LoadingGrid count={3} />
            ) : isError ? (
                <QueryErrorState onRetry={() => void refetch()} />
            ) : profiles.length === 0 ? (
                <EmptyState
                    title="No profiles yet"
                    description="Create your first profile to start matching against job opportunities."
                    actionLabel="Create profile"
                    actionHref="/profiles/new"
                />
            ) : (
                <div className="grid gap-6 md:grid-cols-2">
                    {profiles.map((profile) => {
                        const readiness = calculateProfileReadiness(profile);
                        return (
                            <div key={profile.id} className="bg-white rounded-2xl shadow p-6">
                                <div className="flex items-start justify-between gap-4">
                                    <div>
                                        <Link
                                            to={`/profiles/${profile.id}`}
                                            className="text-2xl font-semibold hover:text-indigo-600"
                                        >
                                            {profile.fullName}
                                        </Link>
                                        <p className="text-slate-500">{profile.professionalTitle}</p>
                                    </div>
                                    <Badge tone={readiness.score >= 80 ? "success" : "warning"}>
                                        {readiness.score}% ready
                                    </Badge>
                                </div>

                                <p className="mt-2 text-slate-500">{profile.email}</p>
                                <p className="mt-4 line-clamp-3">{profile.summary}</p>

                                <div className="flex flex-wrap gap-2 mt-5">
                                    <Link
                                        to={`/profiles/${profile.id}`}
                                        className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                                    >
                                        View
                                    </Link>
                                    <Link
                                        to={`/profiles/${profile.id}/edit`}
                                        className="border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-100"
                                    >
                                        Edit
                                    </Link>
                                    <button
                                        onClick={() => handleDelete(profile.id)}
                                        className="bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                                    >
                                        Delete
                                    </button>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </AppLayout>
    );
}
