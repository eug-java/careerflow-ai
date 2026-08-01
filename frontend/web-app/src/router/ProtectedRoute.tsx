import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import type { ReactNode } from "react";
import { ensureValidAccessToken } from "../api/authApi";

interface Props {
    children: ReactNode;
}

export default function ProtectedRoute({ children }: Props) {
    const [ready, setReady] = useState(false);
    const [allowed, setAllowed] = useState(false);

    useEffect(() => {
        let active = true;

        ensureValidAccessToken()
            .then((token) => {
                if (!active) {
                    return;
                }
                setAllowed(Boolean(token));
                setReady(true);
            })
            .catch(() => {
                if (!active) {
                    return;
                }
                setAllowed(false);
                setReady(true);
            });

        return () => {
            active = false;
        };
    }, []);

    if (!ready) {
        return null;
    }

    if (!allowed) {
        return <Navigate to="/login" replace />;
    }

    return children;
}
