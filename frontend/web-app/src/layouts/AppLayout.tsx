import type { ReactNode } from "react";
import { Link } from "react-router-dom";

interface Props {
    children: ReactNode;
}

export default function AppLayout({ children }: Props) {
    return (
        <div className="min-h-screen flex">
            <aside className="w-64 bg-slate-900 text-white p-6">
                <h1 className="text-2xl font-bold mb-8">
                    CareerFlow AI
                </h1>

                <nav className="flex flex-col gap-4">
                    <Link to="/">Dashboard</Link>
                    <Link to="/profiles">Profiles</Link>
                    <Link to="/jobs">Jobs</Link>
                    <Link to="/documents">Documents</Link>
                </nav>

                <button
                    onClick={handleLogout}
                    className="mt-8 bg-slate-700 rounded-xl px-4 py-2 text-left hover:bg-slate-600"
                >
                    Logout
                </button>
            </aside>

            <main className="flex-1 p-8">
                {children}
            </main>
        </div>
    );
}

function handleLogout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "/login";
}

