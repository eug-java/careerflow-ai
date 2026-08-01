import { apiClient } from "./client";
import type { Experience, LocationPreference, Skill } from "./profileApi";

export interface ParsedResume {
    fullName?: string;
    professionalTitle?: string;
    email?: string;
    phone?: string;
    location?: string;
    locationPreference?: LocationPreference;
    summary?: string;
    skills?: Skill[];
    experiences?: Experience[];
}

export async function parseResumeText(text: string): Promise<ParsedResume> {
    const response = await apiClient.post<ParsedResume>("/api/v1/generations/profiles/parse", { text });
    return response.data;
}

export async function parseResumeFile(file: File): Promise<ParsedResume> {
    const formData = new FormData();
    formData.append("file", file);

    const response = await apiClient.post<ParsedResume>(
        "/api/v1/generations/profiles/parse-file",
        formData,
        {
            headers: { "Content-Type": "multipart/form-data" },
        }
    );

    return response.data;
}
