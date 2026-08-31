import { useState, type ReactNode } from "react";
import { NavLink } from "react-router-dom";
import { logout } from "../api/authApi";

interface Props {
    children: ReactNode;
}

const navClass = ({ isActive }: { isActive: boolean }) =>
    isActive
        ? "text-white font-medium bg-slate-800 rounded-lg px-3 py-2"
        : "text-slate-300 hover:text-white px-3 py-2 rounded-lg hover:bg-slate-800";

const NAV_ITEMS = [
    { to: "/", end: true, label: "Dashboard" },
    { to: "/profiles", label: "Profiles" },
    { to: "/jobs", label: "Jobs" },
    { to: "/applications", label: "Applications" },
    { to: "/matches", label: "Matches" },
    { to: "/workflows", label: "Workflows" },
    { to: "/insights", label: "Skill gaps" },
    { to: "/documents", label: "Documents" },
    { to: "/email", label: "Email" },
    { to: "/email/settings", label: "Email Settings" },
    { to: "/settings/ai", label: "AI Settings" },
] as const;

function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
    return (
        <nav className="flex flex-col gap-1">
            {NAV_ITEMS.map((item) => (
                <NavLink
                    key={item.to}
                    to={item.to}
                    end={"end" in item ? item.end : false}
                    className={navClass}
                    onClick={onNavigate}
                >
                    {item.label}
                </NavLink>
            ))}
        </nav>
    );
}

export default function AppLayout({ children }: Props) {
    const [mobileOpen, setMobileOpen] = useState(false);

    return (
        <div className="min-h-screen flex">
            <aside className="hidden lg:flex w-64 bg-slate-900 text-white p-6 flex-col">
                <h1 className="text-2xl font-bold mb-8">CareerFlow AI</h1>
                <SidebarNav />
                <button
                    onClick={handleLogout}
                    className="mt-auto bg-slate-700 rounded-xl px-4 py-2 text-left hover:bg-slate-600"
                >
                    Logout
                </button>
            </aside>

            {mobileOpen && (
                <div className="lg:hidden fixed inset-0 z-40">
                    <button
                        type="button"
                        className="absolute inset-0 bg-black/40"
                        aria-label="Close menu"
                        onClick={() => setMobileOpen(false)}
                    />
                    <aside className="relative w-72 max-w-[85vw] h-full bg-slate-900 text-white p-6 flex flex-col">
                        <div className="flex items-center justify-between mb-8">
                            <h1 className="text-xl font-bold">CareerFlow AI</h1>
                            <button
                                type="button"
                                onClick={() => setMobileOpen(false)}
                                className="text-slate-300 hover:text-white"
                            >
                                Close
                            </button>
                        </div>
                        <SidebarNav onNavigate={() => setMobileOpen(false)} />
                        <button
                            onClick={handleLogout}
                            className="mt-auto bg-slate-700 rounded-xl px-4 py-2 text-left hover:bg-slate-600"
                        >
                            Logout
                        </button>
                    </aside>
                </div>
            )}

            <div className="flex-1 min-w-0">
                <header className="lg:hidden sticky top-0 z-30 bg-white border-b border-slate-200 px-4 py-3 flex items-center justify-between">
                    <button
                        type="button"
                        onClick={() => setMobileOpen(true)}
                        className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
                    >
                        Menu
                    </button>
                    <span className="font-semibold text-slate-800">CareerFlow AI</span>
                    <div className="w-14" />
                </header>
                <main className="p-4 sm:p-6 lg:p-8">{children}</main>
            </div>
        </div>
    );
}

function handleLogout() {
    void logout().finally(() => {
        window.location.href = "/login";
    });
}
