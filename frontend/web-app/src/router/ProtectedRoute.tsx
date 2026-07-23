import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { isTokenExpired } from "../api/client";

interface Props {
    children: ReactNode;
}

export default function ProtectedRoute({ children }: Props) {
    const token = localStorage.getItem("accessToken");

    if (!token || isTokenExpired(token)) {
        return <Navigate to="/login" replace />;
    }

    return children;
}
