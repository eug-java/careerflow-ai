import { apiClient } from "./client";

export interface StartWorkflowRequest {
    profileId: string;
    jobId: string;
    documentType: "COVER_LETTER" | "RESUME";
}

export async function startDocumentGenerationWorkflow(
    request: StartWorkflowRequest
) {
    const response = await apiClient.post(
        "/api/v1/workflows/document-generation",
        request
    );

    return response.data;
}

export interface BatchWorkflowRequest {
    profileId: string;
    jobIds: string[];
    documentType: "COVER_LETTER" | "RESUME";
}

export interface BatchWorkflowResponse {
    workflows: Array<{
        processInstanceKey: number;
        processId: string;
    }>;
}

export async function startBatchDocumentGenerationWorkflow(
    request: BatchWorkflowRequest
): Promise<BatchWorkflowResponse> {
    const response = await apiClient.post<BatchWorkflowResponse>(
        "/api/v1/workflows/document-generation/batch",
        request
    );

    return response.data;
}

export interface WorkflowStatus {
    processInstanceKey: number;
    processId: string;
    status: string;
    message: string;
}

export interface WorkflowListItem extends WorkflowStatus {
    updatedAt: string;
}

export async function fetchWorkflowStatus(
    processInstanceKey: number
): Promise<WorkflowStatus> {
    const response = await apiClient.get(
        `/api/v1/workflows/${processInstanceKey}/status`
    );

    return response.data;
}

export async function fetchWorkflows(status?: string): Promise<WorkflowListItem[]> {
    const response = await apiClient.get<WorkflowListItem[]>("/api/v1/workflows", {
        params: status ? { status } : undefined,
    });

    return response.data;
}
