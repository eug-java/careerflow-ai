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

export interface WorkflowStatus {
    processInstanceKey: number;
    processId: string;
    status: string;
    message: string;
}

export async function fetchWorkflowStatus(
    processInstanceKey: number
): Promise<WorkflowStatus> {

    const response = await apiClient.get(
        `/api/v1/workflows/${processInstanceKey}/status`
    );

    return response.data;
}