import { apiClient } from "./client";

export interface AiAccount {
    provider: string | null;
    preferredModel: string;
    apiKeyHint: string | null;
    updatedAt: string | null;
    configured: boolean;
}

export interface UpsertAiAccountRequest {
    apiKey: string;
    provider?: string;
    preferredModel?: string;
}

export interface AiConnectionTestResponse {
    success: boolean;
    message: string;
}

export const AI_MODEL_OPTIONS = [
    { value: "gpt-4o-mini", label: "GPT-4o mini (recommended)" },
    { value: "gpt-4o", label: "GPT-4o" },
    { value: "gpt-4.1-mini", label: "GPT-4.1 mini" },
] as const;

export async function fetchAiAccount(): Promise<AiAccount> {
    const response = await apiClient.get<AiAccount>("/api/v1/ai/account");
    return response.data;
}

export async function upsertAiAccount(request: UpsertAiAccountRequest): Promise<AiAccount> {
    const response = await apiClient.put<AiAccount>("/api/v1/ai/account", request);
    return response.data;
}

export async function deleteAiAccount(): Promise<void> {
    await apiClient.delete("/api/v1/ai/account");
}

export async function testAiConnection(
    request: UpsertAiAccountRequest
): Promise<AiConnectionTestResponse> {
    const response = await apiClient.post<AiConnectionTestResponse>(
        "/api/v1/ai/account/test",
        request
    );
    return response.data;
}
