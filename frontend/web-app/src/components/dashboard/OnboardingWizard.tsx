import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { fetchAiAccount } from "../../api/aiApi";
import { useDashboardQueries } from "../../hooks/useDashboardQueries";

const DISMISS_KEY = "careerflow-onboarding-dismissed";

export function OnboardingWizard() {
    const { profiles, jobs, matches, isLoading } = useDashboardQueries();
    const aiQuery = useQuery({
        queryKey: ["ai-account"],
        queryFn: fetchAiAccount,
    });

    if (isLoading || aiQuery.isLoading) {
        return null;
    }

    if (localStorage.getItem(DISMISS_KEY) === "true") {
        return null;
    }

    const aiConfigured = aiQuery.data?.configured ?? false;
    const hasProfile = profiles.length > 0;
    const hasJob = jobs.length > 0;
    const hasMatch = matches.length > 0;

    if (aiConfigured && hasProfile && hasJob && hasMatch) {
        return null;
    }

    const steps = [
        {
            id: "ai",
            label: "Connect OpenAI key",
            done: aiConfigured,
            href: "/settings/ai",
        },
        {
            id: "profile",
            label: "Create your profile",
            done: hasProfile,
            href: "/profiles/new",
        },
        {
            id: "job",
            label: "Add a target job",
            done: hasJob,
            href: "/jobs/new",
        },
        {
            id: "match",
            label: "Run your first match",
            done: hasMatch,
            href: "/jobs",
        },
    ];

    const completedCount = steps.filter((step) => step.done).length;

    function dismiss() {
        localStorage.setItem(DISMISS_KEY, "true");
        window.location.reload();
    }

    return (
        <div className="mb-6 rounded-2xl border border-indigo-200 bg-indigo-50 px-5 py-4">
            <div className="flex items-start justify-between gap-4">
                <div>
                    <p className="font-medium text-indigo-950">Getting started</p>
                    <p className="text-sm text-indigo-900 mt-1">
                        Complete these steps to unlock the full CareerFlow pipeline.
                    </p>
                </div>
                <button
                    type="button"
                    onClick={dismiss}
                    className="text-sm text-indigo-700 hover:text-indigo-900"
                >
                    Dismiss
                </button>
            </div>

            <div className="mt-4 flex items-center gap-3">
                <div className="flex-1 h-2 bg-indigo-100 rounded-full overflow-hidden">
                    <div
                        className="h-full bg-indigo-600 rounded-full transition-all"
                        style={{ width: `${(completedCount / steps.length) * 100}%` }}
                    />
                </div>
                <span className="text-sm font-medium text-indigo-900">
                    {completedCount}/{steps.length}
                </span>
            </div>

            <ol className="mt-4 space-y-2">
                {steps.map((step) => (
                    <li key={step.id} className="flex items-center justify-between gap-3">
                        <span className={step.done ? "text-indigo-700 line-through" : "text-indigo-950"}>
                            {step.label}
                        </span>
                        {step.done ? (
                            <span className="text-xs font-medium text-emerald-700">Done</span>
                        ) : (
                            <Link
                                to={step.href}
                                className="text-sm text-indigo-700 hover:underline"
                            >
                                Start
                            </Link>
                        )}
                    </li>
                ))}
            </ol>
        </div>
    );
}
