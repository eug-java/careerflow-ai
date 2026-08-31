import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { exchangeGitHubCode, storeAuthTokens } from "../api/authApi";

export default function GitHubOAuthCallbackPage() {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const [asyncError, setAsyncError] = useState("");

    const oauthError = searchParams.get("error");
    const code = searchParams.get("code");
    const error = oauthError
        ? "GitHub sign-in was cancelled."
        : !code
          ? "Missing authorization code."
          : asyncError;

    useEffect(() => {
        if (!code || oauthError) {
            return;
        }

        let active = true;

        exchangeGitHubCode(code)
            .then((result) => {
                if (!active) {
                    return;
                }
                storeAuthTokens(result);
                navigate("/", { replace: true });
            })
            .catch(() => {
                if (active) {
                    setAsyncError("GitHub sign-in failed. Please try again.");
                }
            });

        return () => {
            active = false;
        };
    }, [code, navigate, oauthError]);

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-100">
            <div className="bg-white w-full max-w-md rounded-2xl shadow p-8 text-center">
                {error ? (
                    <>
                        <h1 className="text-2xl font-bold mb-4 text-slate-900">Sign-in failed</h1>
                        <p className="text-slate-600 mb-6">{error}</p>
                        <Link
                            to="/login"
                            className="inline-block bg-slate-900 text-white rounded-xl px-6 py-3 hover:bg-slate-700"
                        >
                            Back to sign in
                        </Link>
                    </>
                ) : (
                    <>
                        <h1 className="text-2xl font-bold mb-4 text-slate-900">Signing in with GitHub</h1>
                        <p className="text-slate-600">Completing authentication...</p>
                    </>
                )}
            </div>
        </div>
    );
}
