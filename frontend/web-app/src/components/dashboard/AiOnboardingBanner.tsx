import { Link } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { fetchAiAccount } from "../../api/aiApi";

export function AiOnboardingBanner() {
    const { data, isLoading } = useQuery({
        queryKey: ["ai-account"],
        queryFn: fetchAiAccount,
    });

    if (isLoading || data?.configured) {
        return null;
    }

    return (
        <div className="mb-6 rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4">
            <p className="font-medium text-amber-900">Connect your OpenAI API key</p>
            <p className="text-sm text-amber-800 mt-1">
                AI resume and cover letter generation works best with your own key. You can still use
                deterministic fallbacks without it.
            </p>
            <Link
                to="/settings/ai"
                className="inline-block mt-3 bg-amber-900 text-white px-4 py-2 rounded-xl hover:bg-amber-800 text-sm"
            >
                Configure AI settings
            </Link>
        </div>
    );
}
