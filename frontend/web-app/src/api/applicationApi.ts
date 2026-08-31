import { apiClient } from "./client";

export type ApplicationStatus =
    | "SAVED"
    | "APPLIED"
    | "SCREENING"
    | "INTERVIEW"
    | "OFFER"
    | "REJECTED"
    | "WITHDRAWN";

export interface Application {
    id: string;
    profileId: string;
    jobId: string;
    status: ApplicationStatus;
    notes: string | null;
    appliedAt: string | null;
    createdAt: string;
    updatedAt: string;
}

export interface CreateApplicationRequest {
    profileId: string;
    jobId: string;
    status?: ApplicationStatus;
    notes?: string;
}

export interface UpdateApplicationRequest {
    status?: ApplicationStatus;
    notes?: string;
}

export async function fetchApplications(status?: ApplicationStatus): Promise<Application[]> {
    const response = await apiClient.get<Application[]>("/api/v1/applications", {
        params: status ? { status } : undefined,
    });
    return response.data;
}

export async function createApplication(request: CreateApplicationRequest): Promise<Application> {
    const response = await apiClient.post<Application>("/api/v1/applications", request);
    return response.data;
}

export async function updateApplication(
    id: string,
    request: UpdateApplicationRequest
): Promise<Application> {
    const response = await apiClient.patch<Application>(`/api/v1/applications/${id}`, request);
    return response.data;
}

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
    SAVED: "Saved",
    APPLIED: "Applied",
    SCREENING: "Screening",
    INTERVIEW: "Interview",
    OFFER: "Offer",
    REJECTED: "Rejected",
    WITHDRAWN: "Withdrawn",
};

export const APPLICATION_STATUS_OPTIONS = Object.entries(APPLICATION_STATUS_LABELS).map(
    ([value, label]) => ({ value: value as ApplicationStatus, label })
);
