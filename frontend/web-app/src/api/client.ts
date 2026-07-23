import axios from "axios";

const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const apiClient = axios.create({
    baseURL: apiBaseUrl,
    headers: {
        "Content-Type": "application/json",
    },
});

function decodeJwtExpiry(token: string): number | null {
    try {
        const payload = token.split(".")[1];
        const decoded = JSON.parse(atob(payload)) as { exp?: number };
        return decoded.exp ?? null;
    } catch {
        return null;
    }
}

export function isTokenExpired(token: string | null): boolean {
    if (!token) {
        return true;
    }
    const exp = decodeJwtExpiry(token);
    if (!exp) {
        return false;
    }
    return Date.now() >= exp * 1000;
}

apiClient.interceptors.request.use((config) => {
    const token = localStorage.getItem("accessToken");

    if (token && !isTokenExpired(token)) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
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
