import type { GeneratedDocument } from "../api/documentApi";
import type { Job } from "../api/jobApi";
import type { MatchResult } from "../api/matchingApi";
import type { Profile } from "../api/profileApi";
import type { WorkflowListItem } from "../api/workflowApi";

export interface DashboardSummary {
    profilesCount: number;
    jobsCount: number;
    matchesCount: number;
    documentsCount: number;
    avgMatchScore: number;
    resumeCount: number;
    coverLetterCount: number;
    runningWorkflowsCount: number;
}

export interface ProfileReadiness {
    score: number;
    checks: ReadinessCheck[];
}

export interface ReadinessCheck {
    label: string;
    done: boolean;
    href?: string;
}

export interface ActivityItem {
    id: string;
    type: "match" | "document" | "workflow";
    title: string;
    subtitle: string;
    timestamp: string;
    href?: string;
}

export interface NextBestAction {
    title: string;
    description: string;
    href: string;
    buttonLabel: string;
}

export interface SkillGap {
    matched: string[];
    missing: string[];
}

export function buildDashboardSummary(
    profiles: Profile[],
    jobs: Job[],
    matches: MatchResult[],
    documents: GeneratedDocument[],
    workflows: WorkflowListItem[]
): DashboardSummary {
    const avgMatchScore =
        matches.length === 0
            ? 0
            : Math.round(
                  matches.reduce((sum, match) => sum + Number(match.totalScore), 0) /
                      matches.length
              );

    return {
        profilesCount: profiles.length,
        jobsCount: jobs.length,
        matchesCount: matches.length,
        documentsCount: documents.length,
        avgMatchScore,
        resumeCount: documents.filter((doc) => doc.documentType === "RESUME").length,
        coverLetterCount: documents.filter((doc) => doc.documentType === "COVER_LETTER")
            .length,
        runningWorkflowsCount: workflows.filter((item) => item.status === "RUNNING").length,
    };
}

export function calculateProfileReadiness(profile: Profile | undefined): ProfileReadiness {
    if (!profile) {
        return { score: 0, checks: [] };
    }

    const checks: ReadinessCheck[] = [
        {
            label: "Full name and professional title",
            done: Boolean(profile.fullName?.trim() && profile.professionalTitle?.trim()),
            href: `/profiles/${profile.id}/edit`,
        },
        {
            label: "Email and location",
            done: Boolean(profile.email?.trim() && profile.location?.trim()),
            href: `/profiles/${profile.id}/edit`,
        },
        {
            label: "Summary (200+ characters)",
            done: (profile.summary?.trim().length ?? 0) >= 200,
            href: `/profiles/${profile.id}/edit`,
        },
        {
            label: "At least 3 skills",
            done: (profile.skills?.length ?? 0) >= 3,
            href: `/profiles/${profile.id}`,
        },
        {
            label: "At least 1 work experience",
            done: (profile.experiences?.length ?? 0) >= 1,
            href: `/profiles/${profile.id}`,
        },
    ];

    const doneCount = checks.filter((check) => check.done).length;
    const score = Math.round((doneCount / checks.length) * 100);

    return { score, checks };
}

export function getTopMatches(
    matches: MatchResult[],
    jobs: Job[],
    profiles: Profile[],
    limit = 5
) {
    return [...matches]
        .sort((a, b) => Number(b.totalScore) - Number(a.totalScore))
        .slice(0, limit)
        .map((match) => ({
            match,
            job: jobs.find((job) => job.id === match.jobId),
            profile: profiles.find((profile) => profile.id === match.profileId),
        }));
}

export function buildActivityFeed(
    matches: MatchResult[],
    documents: GeneratedDocument[],
    workflows: WorkflowListItem[],
    jobs: Job[],
    profiles: Profile[]
): ActivityItem[] {
    const items: ActivityItem[] = [];

    for (const match of matches) {
        const job = jobs.find((item) => item.id === match.jobId);
        const profile = profiles.find((item) => item.id === match.profileId);
        items.push({
            id: `match-${match.id}`,
            type: "match",
            title: `Match ${Number(match.totalScore).toFixed(0)}% — ${job?.title ?? "Job"}`,
            subtitle: `${profile?.fullName ?? "Profile"} · ${job?.companyName ?? ""}`,
            timestamp: match.createdAt,
            href: "/matches",
        });
    }

    for (const doc of documents) {
        const job = jobs.find((item) => item.id === doc.jobId);
        items.push({
            id: `doc-${doc.id}`,
            type: "document",
            title: `${doc.documentType.replace("_", " ")} generated`,
            subtitle: job ? `${job.title} at ${job.companyName}` : doc.fileName,
            timestamp: doc.createdAt,
            href: "/documents",
        });
    }

    for (const workflow of workflows) {
        items.push({
            id: `wf-${workflow.processInstanceKey}`,
            type: "workflow",
            title: `Workflow ${workflow.status}`,
            subtitle: workflow.message,
            timestamp: workflow.updatedAt,
            href: "/",
        });
    }

    return items.sort(
        (a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
    );
}

export function resolveNextBestAction(
    profiles: Profile[],
    jobs: Job[],
    matches: MatchResult[],
    documents: GeneratedDocument[]
): NextBestAction {
    if (profiles.length === 0) {
        return {
            title: "Create your first profile",
            description: "Add your skills and experience to unlock job matching and AI documents.",
            href: "/profiles/new",
            buttonLabel: "Create profile",
        };
    }

    const primaryProfile = profiles[0];
    const readiness = calculateProfileReadiness(primaryProfile);

    if (readiness.score < 80) {
        return {
            title: "Complete your profile",
            description: `Your profile is ${readiness.score}% ready. Finish the missing sections to improve match quality.`,
            href: `/profiles/${primaryProfile.id}/edit`,
            buttonLabel: "Improve profile",
        };
    }

    if (jobs.length === 0) {
        return {
            title: "Add your first job",
            description: "Paste a job description or create a vacancy manually to start matching.",
            href: "/jobs/new",
            buttonLabel: "Add job",
        };
    }

    if (matches.length === 0) {
        return {
            title: "Calculate your first match",
            description: "Compare your profile against open roles to find the best opportunities.",
            href: "/jobs",
            buttonLabel: "Go to jobs",
        };
    }

    const topMatch = [...matches].sort(
        (a, b) => Number(b.totalScore) - Number(a.totalScore)
    )[0];
    const topJob = jobs.find((job) => job.id === topMatch.jobId);
    const hasDocumentForTop = documents.some(
        (doc) =>
            doc.profileId === topMatch.profileId &&
            doc.jobId === topMatch.jobId
    );

    if (Number(topMatch.totalScore) >= 70 && !hasDocumentForTop) {
        return {
            title: `Generate documents for ${topJob?.title ?? "top match"}`,
            description: `You have a strong ${Number(topMatch.totalScore).toFixed(0)}% match. Create a cover letter or resume next.`,
            href: `/jobs/${topMatch.jobId}`,
            buttonLabel: "Open job",
        };
    }

    if (Number(topMatch.totalScore) < 70) {
        return {
            title: "Improve skills for better matches",
            description: "Your top match is below 70%. Review skill gaps and update your profile.",
            href: `/profiles/${topMatch.profileId}`,
            buttonLabel: "View skill gaps",
        };
    }

    return {
        title: "Review your pipeline",
        description: "You are in good shape. Check recent matches and generated documents.",
        href: "/matches",
        buttonLabel: "View matches",
    };
}

export function computeSkillGap(profile: Profile | undefined, job: Job | undefined): SkillGap {
    if (!profile || !job) {
        return { matched: [], missing: [] };
    }

    const profileSkills = new Set(
        (profile.skills ?? []).map((skill) => skill.name.toLowerCase())
    );
    const requiredSkills = (job.skills ?? []).filter((skill) => skill.required);

    const matched: string[] = [];
    const missing: string[] = [];

    for (const skill of requiredSkills) {
        if (profileSkills.has(skill.name.toLowerCase())) {
            matched.push(skill.name);
        } else {
            missing.push(skill.name);
        }
    }

    return { matched, missing };
}

export function scoreColor(score: number): string {
    if (score >= 70) {
        return "bg-emerald-500";
    }
    if (score >= 40) {
        return "bg-amber-500";
    }
    return "bg-rose-500";
}

export function formatRelativeTime(iso: string): string {
    const date = new Date(iso);
    const diffMs = Date.now() - date.getTime();
    const diffMinutes = Math.floor(diffMs / 60_000);

    if (diffMinutes < 1) {
        return "just now";
    }
    if (diffMinutes < 60) {
        return `${diffMinutes}m ago`;
    }

    const diffHours = Math.floor(diffMinutes / 60);
    if (diffHours < 24) {
        return `${diffHours}h ago`;
    }

    const diffDays = Math.floor(diffHours / 24);
    if (diffDays < 7) {
        return `${diffDays}d ago`;
    }

    return date.toLocaleDateString();
}

export function greetingName(): string {
    const hour = new Date().getHours();
    if (hour < 12) {
        return "Good morning";
    }
    if (hour < 18) {
        return "Good afternoon";
    }
    return "Good evening";
}
