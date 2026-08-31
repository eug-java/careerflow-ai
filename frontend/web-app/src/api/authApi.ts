import { apiClient } from "./client";

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    tokenType: string;
    expiresInSeconds: number;
}

export async function isGitHubOAuthEnabled(): Promise<boolean> {
    try {
        const response = await apiClient.get<{ enabled: boolean }>("/api/v1/auth/oauth/github/enabled");
        return response.data.enabled;
    } catch {
        return false;
    }
}

export async function exchangeGitHubCode(code: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>("/api/v1/auth/oauth/github", { code });
    return response.data;
}

export function buildGitHubAuthorizeUrl(clientId: string, redirectUri: string): string {
    const params = new URLSearchParams({
        client_id: clientId,
        redirect_uri: redirectUri,
        scope: "read:user",
    });
    return `https://github.com/login/oauth/authorize?${params.toString()}`;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>("/api/v1/auth/login", {
        username,
        password,
    });

    return response.data;
}

export async function register(username: string, password: string): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>("/api/v1/auth/register", {
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

export async function logout(): Promise<void> {
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
        try {
            await apiClient.post("/api/v1/auth/logout", { refreshToken });
        } catch {
            // Best-effort server-side revocation; local tokens are cleared regardless.
        }
    }
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
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
