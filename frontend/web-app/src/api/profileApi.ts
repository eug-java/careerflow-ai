import { apiClient } from "./client";

export type LocationPreference = "CITY" | "METRO" | "NATIONWIDE";

export interface Skill {
    id?: string;
    name: string;
    category?: string;
    yearsOfExperience?: number;
}

export interface Experience {
    id?: string;
    companyName: string;
    positionTitle: string;
    location?: string;
    startDate?: string;
    endDate?: string;
    currentPosition?: boolean;
    description?: string;
}

export interface Profile {
    id: string;
    fullName: string;
    professionalTitle: string;
    email: string;
    phone?: string;
    location: string;
    locationPreference?: LocationPreference;
    summary: string;
    skills?: Skill[];
    experiences?: Experience[];
    createdAt?: string;
    updatedAt?: string;
}

export interface CreateProfileRequest {
    fullName: string;
    professionalTitle: string;
    email: string;
    phone: string;
    location: string;
    locationPreference?: LocationPreference;
    summary: string;
    skills?: Skill[];
    experiences?: Experience[];
}

export async function fetchProfiles(): Promise<Profile[]> {
    const response = await apiClient.get("/api/v1/profiles");
    return response.data;
}

export async function createProfile(
    request: CreateProfileRequest
): Promise<Profile> {
    const response = await apiClient.post("/api/v1/profiles", request);
    return response.data;
}

export async function deleteProfile(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/profiles/${id}`);
}

export async function fetchProfileById(id: string): Promise<Profile> {
    const response = await apiClient.get(`/api/v1/profiles/${id}`);
    return response.data;
}

export async function updateProfile(
    id: string,
    request: CreateProfileRequest
): Promise<Profile> {
    const response = await apiClient.put(`/api/v1/profiles/${id}`, request);
    return response.data;
}
