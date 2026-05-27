import { apiClient } from "./client";

export interface Profile {
    id: string;
    fullName: string;
    professionalTitle: string;
    email: string;
    phone?: string;
    location: string;
    summary: string;
}

export interface CreateProfileRequest {
    fullName: string;
    professionalTitle: string;
    email: string;
    phone: string;
    location: string;
    summary: string;
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