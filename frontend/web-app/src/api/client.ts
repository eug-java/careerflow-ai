import axios from "axios";
import { isTokenExpired } from "./tokenUtils";

const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
    baseURL: apiBaseUrl,
    headers: {
        "Content-Type": "application/json",
    },
});

export { isTokenExpired } from "./tokenUtils";

let refreshPromise: Promise<string | null> | null = null;

async function refreshAccessToken(): Promise<string | null> {
    const refreshToken = localStorage.getItem("refreshToken");
    if (!refreshToken) {
        return null;
    }

    try {
        const response = await axios.post(
            `${apiBaseUrl}/api/v1/auth/refresh`,
            { refreshToken },
            { headers: { "Content-Type": "application/json" } }
        );

        localStorage.setItem("accessToken", response.data.accessToken);
        localStorage.setItem("refreshToken", response.data.refreshToken);
        return response.data.accessToken as string;
    } catch {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        return null;
    }
}

async function getValidAccessToken(): Promise<string | null> {
    const token = localStorage.getItem("accessToken");
    if (token && !isTokenExpired(token)) {
        return token;
    }

    if (!refreshPromise) {
        refreshPromise = refreshAccessToken().finally(() => {
            refreshPromise = null;
        });
    }

    return refreshPromise;
}

apiClient.interceptors.request.use(async (config) => {
    const token = await getValidAccessToken();

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config as (typeof error.config & { _retry?: boolean }) | undefined;

        if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
            originalRequest._retry = true;
            const token = await refreshAccessToken();

            if (token) {
                originalRequest.headers.Authorization = `Bearer ${token}`;
                return apiClient(originalRequest);
            }
        }

        if (error.response?.status === 401) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            if (window.location.pathname !== "/login") {
                window.location.href = "/login";
            }
        }

        const message =
            error.response?.data?.message ??
            error.message ??
            "Request failed";

        return Promise.reject(new Error(message));
    }
);
