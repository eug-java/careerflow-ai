import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register, storeAuthTokens } from "../api/authApi";

export default function RegisterPage() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setError("");

        if (password !== confirmPassword) {
            setError("Passwords do not match.");
            return;
        }

        if (password.length < 8) {
            setError("Password must be at least 8 characters.");
            return;
        }

        try {
            const result = await register(username, password);
            storeAuthTokens(result);
            navigate("/");
        } catch {
            setError("Could not create account. Username may already be taken.");
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-slate-100">
            <form
                onSubmit={handleSubmit}
                className="bg-white w-full max-w-md rounded-2xl shadow p-8"
            >
                <h1 className="text-3xl font-bold mb-2">
                    Create account
                </h1>

                <p className="text-slate-500 mb-8">
                    Start your CareerFlow workspace
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
                        autoComplete="username"
                        required
                    />
                </label>

                <label className="block mb-4">
                    <span className="block text-sm font-medium text-slate-700 mb-1">
                        Password
                    </span>
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                        autoComplete="new-password"
                        required
                    />
                </label>

                <label className="block mb-6">
                    <span className="block text-sm font-medium text-slate-700 mb-1">
                        Confirm password
                    </span>
                    <input
                        type="password"
                        value={confirmPassword}
                        onChange={(event) => setConfirmPassword(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                        autoComplete="new-password"
                        required
                    />
                </label>

                <button
                    type="submit"
                    className="w-full bg-slate-900 text-white rounded-xl py-3 hover:bg-slate-700"
                >
                    Create Account
                </button>

                <p className="text-sm text-slate-500 mt-6 text-center">
                    Already have an account?{" "}
                    <Link to="/login" className="text-indigo-600 hover:underline">
                        Sign in
                    </Link>
                </p>
            </form>
        </div>
    );
}
