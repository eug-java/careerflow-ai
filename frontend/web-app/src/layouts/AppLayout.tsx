import type { ReactNode } from "react";
import { NavLink } from "react-router-dom";
import { logout } from "../api/authApi";

interface Props {
    children: ReactNode;
}

const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive
        ? "text-white font-medium bg-slate-800 rounded-lg px-3 py-2"
        : "text-slate-300 hover:text-white px-3 py-2 rounded-lg hover:bg-slate-800";

export default function AppLayout({ children }: Props) {
    return (
        <div className="min-h-screen flex">
            <aside className="w-64 bg-slate-900 text-white p-6 flex flex-col">
                <h1 className="text-2xl font-bold mb-8">CareerFlow AI</h1>

                <nav className="flex flex-col gap-1">
                    <NavLink to="/" end className={navClass}>
                        Dashboard
                    </NavLink>
                    <NavLink to="/profiles" className={navClass}>
                        Profiles
                    </NavLink>
                    <NavLink to="/jobs" className={navClass}>
                        Jobs
                    </NavLink>
                    <NavLink to="/applications" className={navClass}>
                        Applications
                    </NavLink>
                    <NavLink to="/matches" className={navClass}>
                        Matches
                    </NavLink>
                    <NavLink to="/documents" className={navClass}>
                        Documents
                    </NavLink>
                    <NavLink to="/email" className={navClass}>
                        Email
                    </NavLink>
                    <NavLink to="/email/settings" className={navClass}>
                        Email Settings
                    </NavLink>
                    <NavLink to="/settings/ai" className={navClass}>
                        AI Settings
                    </NavLink>
                </nav>

                <button
                    onClick={handleLogout}
                    className="mt-auto bg-slate-700 rounded-xl px-4 py-2 text-left hover:bg-slate-600"
                >
                    Logout
                </button>
            </aside>

            <main className="flex-1 p-8">{children}</main>
        </div>
    );
}

function handleLogout() {
    void logout().finally(() => {
        window.location.href = "/login";
    });
}
