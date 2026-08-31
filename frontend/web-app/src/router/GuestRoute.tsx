import { useEffect, useState } from "react";
import { Navigate } from "react-router-dom";
import { ensureValidAccessToken } from "../api/authApi";
import { PageLoadingState } from "../components/ui/Card";

interface Props {
    children: React.ReactNode;
}

export default function GuestRoute({ children }: Props) {
    const [ready, setReady] = useState(false);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        let active = true;

        ensureValidAccessToken()
            .then((token) => {
                if (!active) {
                    return;
                }
                setAuthenticated(Boolean(token));
                setReady(true);
            })
            .catch(() => {
                if (!active) {
                    return;
                }
                setAuthenticated(false);
                setReady(true);
            });

        return () => {
            active = false;
        };
    }, []);

    if (!ready) {
        return <PageLoadingState label="Checking session..." />;
    }

    if (authenticated) {
        return <Navigate to="/" replace />;
    }

    return children;
}
