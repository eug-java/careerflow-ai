import { apiClient } from "./client";

export interface Job {
    id: string;
    title: string;
    companyName: string;
    location: string;
    employmentType: string;
    salaryMin: number;
    salaryMax: number;
    currency: string;
    remote: boolean;
    description: string;
    skills?: CreateJobSkillRequest[];
}

export interface CreateJobSkillRequest {
    name: string;
    required: boolean;
}

export interface CreateJobRequest {
    title: string;
    companyName: string;
    location: string;
    employmentType: string;
    salaryMin: number;
    salaryMax: number;
    currency: string;
    remote: boolean;
    description: string;
    skills: CreateJobSkillRequest[];
}

export async function fetchJobs(): Promise<Job[]> {
    const response = await apiClient.get("/api/v1/jobs");
    return response.data;
}

export async function createJob(request: CreateJobRequest): Promise<Job> {
    const response = await apiClient.post("/api/v1/jobs", request);
    return response.data;
}

export async function deleteJob(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/jobs/${id}`);
}

export async function fetchJobById(id: string): Promise<Job> {
    const response = await apiClient.get(`/api/v1/jobs/${id}`);
    return response.data;
}

export async function updateJob(
    id: string,
    request: CreateJobRequest
): Promise<Job> {
    const response = await apiClient.put(`/api/v1/jobs/${id}`, request);
    return response.data;
}

export interface ParseJobDescriptionRequest {
    text: string;
}

export interface ParsedJobSkill {
    name: string;
    required: boolean;
}

export interface ParsedJobDescription {
    title: string;
    companyName: string;
    location: string;
    employmentType: string;
    salaryMin: number | null;
    salaryMax: number | null;
    currency: string;
    remote: boolean;
    description: string;
    skills: ParsedJobSkill[];
}

export async function parseJobDescriptionWithAi(
    request: ParseJobDescriptionRequest
): Promise<ParsedJobDescription> {
    const response = await apiClient.post(
        "/api/v1/generations/jobs/parse",
        request
    );

    return response.data;
}