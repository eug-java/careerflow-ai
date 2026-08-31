import { useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import AppLayout from "../layouts/AppLayout";
import {
    APPLICATION_STATUS_LABELS,
    APPLICATION_STATUS_OPTIONS,
    fetchApplications,
    updateApplication,
    type ApplicationStatus,
} from "../api/applicationApi";
import { useJobsQuery, useProfilesQuery } from "../hooks/useDashboardQueries";
import { Badge, Card, EmptyState, LoadingGrid, QueryErrorState } from "../components/ui/Card";
import { useToast } from "../hooks/useToast";

const PIPELINE_STATUSES: ApplicationStatus[] = [
    "SAVED",
    "APPLIED",
    "SCREENING",
    "INTERVIEW",
    "OFFER",
    "REJECTED",
    "WITHDRAWN",
];

export default function ApplicationsPage() {
    const [statusFilter, setStatusFilter] = useState<ApplicationStatus | "ALL">("ALL");
    const queryClient = useQueryClient();
    const { pushToast } = useToast();

    const applicationsQuery = useQuery({
        queryKey: ["applications"],
        queryFn: () => fetchApplications(),
    });

    const jobsQuery = useJobsQuery();
    const profilesQuery = useProfilesQuery();

    const jobsById = useMemo(
        () => new Map((jobsQuery.data ?? []).map((job) => [job.id, job])),
        [jobsQuery.data]
    );
    const profilesById = useMemo(
        () => new Map((profilesQuery.data ?? []).map((profile) => [profile.id, profile])),
        [profilesQuery.data]
    );

    const updateMutation = useMutation({
        mutationFn: ({ id, status }: { id: string; status: ApplicationStatus }) =>
            updateApplication(id, { status }),
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ["applications"] });
            pushToast("success", "Application status updated.");
        },
        onError: () => pushToast("error", "Could not update application."),
    });

    if (applicationsQuery.isLoading || jobsQuery.isLoading || profilesQuery.isLoading) {
        return (
            <AppLayout>
                <LoadingGrid count={4} />
            </AppLayout>
        );
    }

    if (applicationsQuery.isError || jobsQuery.isError || profilesQuery.isError) {
        return (
            <AppLayout>
                <QueryErrorState
                    title="Unable to load applications"
                    onRetry={() => {
                        void applicationsQuery.refetch();
                        void jobsQuery.refetch();
                        void profilesQuery.refetch();
                    }}
                />
            </AppLayout>
        );
    }

    const applications = applicationsQuery.data ?? [];
    const filteredApplications =
        statusFilter === "ALL"
            ? applications
            : applications.filter((item) => item.status === statusFilter);
    const counts = PIPELINE_STATUSES.reduce<Record<string, number>>((acc, status) => {
        acc[status] = applications.filter((item) => item.status === status).length;
        return acc;
    }, {});

    return (
        <AppLayout>
            <div className="mb-8 flex flex-col lg:flex-row lg:items-end lg:justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-bold">Application Tracker</h1>
                    <p className="text-slate-500 mt-2">
                        Track where each profile/job pair stands in your hiring pipeline.
                    </p>
                </div>
                <Link
                    to="/jobs"
                    className="inline-flex items-center justify-center bg-slate-900 text-white rounded-xl px-4 py-2 hover:bg-slate-700"
                >
                    Track from Jobs
                </Link>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-7 gap-3 mb-6">
                {PIPELINE_STATUSES.map((status) => (
                    <Card key={status} className="text-center py-3">
                        <p className="text-xs uppercase tracking-wide text-slate-500">
                            {APPLICATION_STATUS_LABELS[status]}
                        </p>
                        <p className="text-2xl font-bold mt-1">{counts[status] ?? 0}</p>
                    </Card>
                ))}
            </div>

            <div className="mb-4">
                <label className="text-sm text-slate-600 mr-2">Filter</label>
                <select
                    value={statusFilter}
                    onChange={(event) =>
                        setStatusFilter(event.target.value as ApplicationStatus | "ALL")
                    }
                    className="border border-slate-300 rounded-xl px-3 py-2"
                >
                    <option value="ALL">All statuses</option>
                    {APPLICATION_STATUS_OPTIONS.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
            </div>

            {filteredApplications.length === 0 ? (
                <EmptyState
                    title="No tracked applications yet"
                    description="Open a job and click Track application to start your pipeline."
                    actionLabel="Browse jobs"
                    actionHref="/jobs"
                />
            ) : (
                <div className="space-y-4">
                    {filteredApplications.map((application) => {
                        const job = jobsById.get(application.jobId);
                        const profile = profilesById.get(application.profileId);

                        return (
                            <Card key={application.id}>
                                <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
                                    <div>
                                        <div className="flex items-center gap-2 flex-wrap">
                                            <h2 className="text-lg font-semibold">
                                                {job?.title ?? "Unknown job"}
                                            </h2>
                                            {job?.companyName && (
                                                <Badge>{job.companyName}</Badge>
                                            )}
                                        </div>
                                        <p className="text-sm text-slate-500 mt-1">
                                            Profile: {profile?.fullName ?? application.profileId}
                                        </p>
                                        {application.appliedAt && (
                                            <p className="text-xs text-slate-400 mt-1">
                                                Applied {new Date(application.appliedAt).toLocaleDateString()}
                                            </p>
                                        )}
                                    </div>

                                    <div className="flex items-center gap-3">
                                        <select
                                            value={application.status}
                                            disabled={updateMutation.isPending}
                                            onChange={(event) =>
                                                updateMutation.mutate({
                                                    id: application.id,
                                                    status: event.target.value as ApplicationStatus,
                                                })
                                            }
                                            className="border border-slate-300 rounded-xl px-3 py-2"
                                        >
                                            {APPLICATION_STATUS_OPTIONS.map((option) => (
                                                <option key={option.value} value={option.value}>
                                                    {option.label}
                                                </option>
                                            ))}
                                        </select>
                                        {job && (
                                            <Link
                                                to={`/jobs/${job.id}`}
                                                className="text-sm text-indigo-600 hover:underline"
                                            >
                                                Open job
                                            </Link>
                                        )}
                                    </div>
                                </div>
                            </Card>
                        );
                    })}
                </div>
            )}
        </AppLayout>
    );
}
