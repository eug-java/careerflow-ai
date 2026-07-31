import { apiClient } from "./client";

export interface MatchResult {
    id: string;
    profileId: string;
    jobId: string;
    totalScore: number;
    skillsScore: number;
    locationScore: number;
    salaryScore: number;
    explanation: string;
    createdAt: string;
}

export async function calculateMatch(profileId: string, jobId: string): Promise<MatchResult> {
    const response = await apiClient.post<MatchResult>("/api/v1/matches", {
        profileId,
        jobId,
    });

    return response.data;
}
