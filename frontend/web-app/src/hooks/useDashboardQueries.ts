import { useQuery } from "@tanstack/react-query";
import { fetchDocuments } from "../api/documentApi";
import { fetchJobs } from "../api/jobApi";
import { fetchMatches } from "../api/matchingApi";
import { fetchProfiles } from "../api/profileApi";
import { fetchWorkflows } from "../api/workflowApi";

export function useProfilesQuery() {
    return useQuery({
        queryKey: ["profiles"],
        queryFn: fetchProfiles,
    });
}

export function useJobsQuery() {
    return useQuery({
        queryKey: ["jobs"],
        queryFn: fetchJobs,
    });
}

export function useMatchesQuery(profileId?: string, jobId?: string) {
    return useQuery({
        queryKey: ["matches", profileId ?? "all", jobId ?? "all"],
        queryFn: () => fetchMatches({ profileId, jobId }),
    });
}

export function useDocumentsQuery(profileId?: string, jobId?: string) {
    return useQuery({
        queryKey: ["documents", profileId ?? "all", jobId ?? "all"],
        queryFn: () => fetchDocuments({ profileId, jobId }),
    });
}

export function useWorkflowsQuery(status?: string) {
    return useQuery({
        queryKey: ["workflows", status ?? "all"],
        queryFn: () => fetchWorkflows(status),
        refetchInterval: 5000,
    });
}

export function useDashboardQueries() {
    const profiles = useProfilesQuery();
    const jobs = useJobsQuery();
    const matches = useMatchesQuery();
    const documents = useDocumentsQuery();
    const workflows = useWorkflowsQuery();

    const isLoading =
        profiles.isLoading ||
        jobs.isLoading ||
        matches.isLoading ||
        documents.isLoading ||
        workflows.isLoading;

    const isError =
        profiles.isError ||
        jobs.isError ||
        matches.isError ||
        documents.isError ||
        workflows.isError;

    return {
        profiles: profiles.data ?? [],
        jobs: jobs.data ?? [],
        matches: matches.data ?? [],
        documents: documents.data ?? [],
        workflows: workflows.data ?? [],
        isLoading,
        isError,
        refetchAll: () =>
            Promise.all([
                profiles.refetch(),
                jobs.refetch(),
                matches.refetch(),
                documents.refetch(),
                workflows.refetch(),
            ]),
    };
}
