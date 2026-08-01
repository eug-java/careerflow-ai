import { apiClient } from "./client";

export interface MatchResult {
    id: string;
    profileId: string;
    jobId: string;
    totalScore: number;
    skillsScore: number;
    locationScore: number;
    salaryScore: number;
    experienceScore?: number;
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

export interface FetchMatchesParams {
    profileId?: string;
    jobId?: string;
}

export async function fetchMatches(params: FetchMatchesParams = {}): Promise<MatchResult[]> {
    const response = await apiClient.get<MatchResult[]>("/api/v1/matches", {
        params,
    });

    return response.data;
}

export async function fetchMatchById(id: string): Promise<MatchResult> {
    const response = await apiClient.get<MatchResult>(`/api/v1/matches/${id}`);
    return response.data;
}
