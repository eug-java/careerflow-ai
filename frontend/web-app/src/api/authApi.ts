import { apiClient } from "./client";

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresInSeconds: number;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>("/api/v1/auth/login", {
        username,
        password,
    });

    return response.data;
}

export async function refreshAccessToken(refreshToken: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>("/api/v1/auth/refresh", {
        refreshToken,
    });

    return response.data;
}

export function storeAuthTokens(result: LoginResponse) {
    localStorage.setItem("accessToken", result.accessToken);
    localStorage.setItem("refreshToken", result.refreshToken);
}

export async function ensureValidAccessToken(): Promise<string | null> {
    const accessToken = localStorage.getItem("accessToken");
    const refreshToken = localStorage.getItem("refreshToken");

    if (accessToken && !isExpired(accessToken)) {
        return accessToken;
    }

    if (!refreshToken) {
        return null;
    }

    try {
        const refreshed = await refreshAccessToken(refreshToken);
        storeAuthTokens(refreshed);
        return refreshed.accessToken;
    } catch {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        return null;
    }
}

function isExpired(token: string): boolean {
    try {
        const payload = JSON.parse(atob(token.split(".")[1])) as { exp?: number };
        return payload.exp != null && Date.now() >= payload.exp * 1000;
    } catch {
        return true;
    }
}
