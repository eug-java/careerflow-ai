import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { buildGitHubAuthorizeUrl, isGitHubOAuthEnabled, login, storeAuthTokens } from "../api/authApi";

const githubClientId = import.meta.env.VITE_GITHUB_OAUTH_CLIENT_ID as string | undefined;
const githubRedirectUri = `${window.location.origin}/oauth/github/callback`;

export default function LoginPage() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [githubOAuthEnabled, setGitHubOAuthEnabled] = useState(false);

    useEffect(() => {
        let active = true;

        isGitHubOAuthEnabled()
            .then((enabled) => {
                if (active) {
                    setGitHubOAuthEnabled(enabled && Boolean(githubClientId));
                }
            })
            .catch(() => {
                if (active) {
                    setGitHubOAuthEnabled(false);
                }
            });

        return () => {
            active = false;
        };
    }, []);

    function handleGitHubSignIn() {
        if (!githubClientId) {
            return;
        }

        window.location.assign(buildGitHubAuthorizeUrl(githubClientId, githubRedirectUri));
    }

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setError("");

        try {
            const result = await login(username, password);
            storeAuthTokens(result);
            navigate("/");
        } catch {
            setError("Invalid username or password.");
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-100">
            <form
                onSubmit={handleSubmit}
                className="bg-white w-full max-w-md rounded-2xl shadow p-8"
            >
                <h1 className="text-3xl font-bold mb-2">
                    CareerFlow AI
                </h1>

                <p className="text-slate-500 mb-8">
                    Sign in to continue. Demo account: demo / demo
                </p>

                {error && (
                    <div className="bg-red-100 text-red-700 rounded-xl px-4 py-3 mb-4">
                        {error}
                    </div>
                )}

                <label className="block mb-4">
          <span className="block text-sm font-medium text-slate-700 mb-1">
            Username
          </span>

                    <input
                        value={username}
                        onChange={(event) => setUsername(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                    />
                </label>

                <label className="block mb-6">
          <span className="block text-sm font-medium text-slate-700 mb-1">
            Password
          </span>

                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                    />
                </label>

                <button
                    type="submit"
                    className="w-full bg-slate-900 text-white rounded-xl py-3 hover:bg-slate-700"
                >
                    Sign In
                </button>

                {githubOAuthEnabled && (
                    <>
                        <div className="flex items-center gap-3 my-6">
                            <div className="h-px flex-1 bg-slate-200" />
                            <span className="text-xs uppercase tracking-wide text-slate-400">or</span>
                            <div className="h-px flex-1 bg-slate-200" />
                        </div>

                        <button
                            type="button"
                            onClick={handleGitHubSignIn}
                            className="w-full border border-slate-300 rounded-xl py-3 hover:bg-slate-50"
                        >
                            Continue with GitHub
                        </button>
                    </>
                )}

                <p className="text-sm text-slate-500 mt-6 text-center">
                    New here?{" "}
                    <Link to="/register" className="text-indigo-600 hover:underline">
                        Create an account
                    </Link>
                </p>
            </form>
        </div>
    );
}