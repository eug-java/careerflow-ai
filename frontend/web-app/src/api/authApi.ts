import { apiClient } from "./client";

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresInSeconds: number;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post("/api/v1/auth/login", {
        username,
        password,
    });

    return response.data;
}