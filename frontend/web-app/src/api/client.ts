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
