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

export type DocumentType = "COVER_LETTER" | "RESUME";

export interface GeneratePreviewRequest {
    profileId: string;
    jobId: string;
    documentType: DocumentType;
}

export interface GeneratePreviewResponse {
    profileId: string;
    jobId: string;
    documentType: DocumentType;
    generationMode: string;
    model: string;
    content: string;
}

export async function generateDocumentPreview(
    request: GeneratePreviewRequest
): Promise<GeneratePreviewResponse> {
    const response = await apiClient.post<GeneratePreviewResponse>(
        "/api/v1/generations/content",
        request
    );
    return response.data;
}
