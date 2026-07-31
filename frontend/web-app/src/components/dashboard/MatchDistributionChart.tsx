import type { MatchResult } from "../../api/matchingApi";
import { Card } from "../ui/Card";

interface Props {
    matches: MatchResult[];
}

function bucketScore(score: number): number {
    if (score >= 80) {
        return 3;
    }
    if (score >= 60) {
        return 2;
    }
    if (score >= 40) {
        return 1;
    }
    return 0;
}

export function MatchDistributionChart({ matches }: Props) {
    const buckets = [
        { label: "0–39%", count: 0, color: "bg-rose-500" },
        { label: "40–59%", count: 0, color: "bg-amber-500" },
        { label: "60–79%", count: 0, color: "bg-blue-500" },
        { label: "80–100%", count: 0, color: "bg-emerald-500" },
    ];

    for (const match of matches) {
        buckets[bucketScore(Number(match.totalScore))].count += 1;
    }

    const max = Math.max(1, ...buckets.map((bucket) => bucket.count));

    return (
        <Card>
            <h2 className="text-xl font-semibold mb-4">Match distribution</h2>
            <div className="space-y-3">
                {buckets.map((bucket) => (
                    <div key={bucket.label}>
                        <div className="flex justify-between text-sm mb-1">
                            <span className="text-slate-600">{bucket.label}</span>
                            <span>{bucket.count}</span>
                        </div>
                        <div className="h-2 bg-slate-100 rounded-full overflow-hidden">
                            <div
                                className={`h-full rounded-full ${bucket.color}`}
                                style={{ width: `${(bucket.count / max) * 100}%` }}
                            />
                        </div>
                    </div>
                ))}
            </div>
        </Card>
    );
}
