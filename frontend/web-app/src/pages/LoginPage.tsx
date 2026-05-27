import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authApi";

export default function LoginPage() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("demo");
    const [password, setPassword] = useState("demo");
    const [error, setError] = useState("");

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setError("");

        try {
            const result = await login(username, password);
            localStorage.setItem("accessToken", result.accessToken);
            navigate("/profiles");
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
                    Sign in to continue
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
            </form>
        </div>
    );
}