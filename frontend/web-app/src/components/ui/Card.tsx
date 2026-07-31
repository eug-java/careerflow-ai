import type { ReactNode } from "react";

interface CardProps {
    children: ReactNode;
    className?: string;
}

export function Card({ children, className = "" }: CardProps) {
    return (
        <div className={`bg-white rounded-2xl shadow p-6 ${className}`}>{children}</div>
    );
}

interface KpiCardProps {
    label: string;
    value: string | number;
    hint?: string;
    href?: string;
    accent?: string;
}

export function KpiCard({ label, value, hint, href, accent = "text-slate-900" }: KpiCardProps) {
    const content = (
        <>
            <p className="text-sm text-slate-500">{label}</p>
            <p className={`text-3xl font-bold mt-2 ${accent}`}>{value}</p>
            {hint && <p className="text-xs text-slate-400 mt-2">{hint}</p>}
        </>
    );

    if (href) {
        return (
            <a
                href={href}
                className="bg-white rounded-2xl shadow p-6 hover:shadow-md transition block"
            >
                {content}
            </a>
        );
    }

    return <div className="bg-white rounded-2xl shadow p-6">{content}</div>;
}

export function EmptyState({
    title,
    description,
    actionLabel,
    actionHref,
}: {
    title: string;
    description: string;
    actionLabel?: string;
    actionHref?: string;
}) {
    return (
        <div className="bg-white rounded-2xl shadow p-10 text-center">
            <h3 className="text-xl font-semibold text-slate-800">{title}</h3>
            <p className="text-slate-500 mt-2 max-w-md mx-auto">{description}</p>
            {actionLabel && actionHref && (
                <a
                    href={actionHref}
                    className="inline-block mt-6 bg-slate-900 text-white px-5 py-2 rounded-xl hover:bg-slate-700"
                >
                    {actionLabel}
                </a>
            )}
        </div>
    );
}

export function LoadingGrid({ count = 4 }: { count?: number }) {
    return (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            {Array.from({ length: count }).map((_, index) => (
                <div
                    key={index}
                    className="bg-white rounded-2xl shadow p-6 animate-pulse h-28"
                />
            ))}
        </div>
    );
}

export function ScoreBar({ label, score }: { label: string; score: number }) {
    const value = Math.max(0, Math.min(100, score));

    return (
        <div>
            <div className="flex justify-between text-sm mb-1">
                <span className="text-slate-600">{label}</span>
                <span className="font-medium">{value.toFixed(0)}%</span>
            </div>
            <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                <div
                    className={`h-full rounded-full ${
                        value >= 70 ? "bg-emerald-500" : value >= 40 ? "bg-amber-500" : "bg-rose-500"
                    }`}
                    style={{ width: `${value}%` }}
                />
            </div>
        </div>
    );
}

export function Badge({
    children,
    tone = "default",
}: {
    children: ReactNode;
    tone?: "default" | "success" | "warning" | "danger" | "info";
}) {
    const tones = {
        default: "bg-slate-100 text-slate-700",
        success: "bg-emerald-100 text-emerald-800",
        warning: "bg-amber-100 text-amber-800",
        danger: "bg-rose-100 text-rose-800",
        info: "bg-blue-100 text-blue-800",
    };

    return (
        <span className={`inline-flex px-2.5 py-1 rounded-full text-xs font-medium ${tones[tone]}`}>
            {children}
        </span>
    );
}
