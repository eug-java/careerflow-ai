import { describe, expect, it } from "vitest";
import {
    buildDashboardSummary,
    calculateProfileReadiness,
    resolveNextBestAction,
} from "./dashboardUtils";
import type { Profile } from "../api/profileApi";

const profile: Profile = {
    id: "p1",
    fullName: "Demo User",
    professionalTitle: "Engineer",
    email: "demo@example.com",
    location: "Remote",
    summary: "x".repeat(220),
    skills: [{ name: "Java" }, { name: "Spring" }, { name: "Kafka" }],
    experiences: [{ companyName: "Acme", positionTitle: "Dev" }],
};

describe("dashboardUtils", () => {
    it("calculates profile readiness", () => {
        const readiness = calculateProfileReadiness(profile);
        expect(readiness.score).toBeGreaterThanOrEqual(80);
    });

    it("builds dashboard summary", () => {
        const summary = buildDashboardSummary([profile], [], [], [], []);
        expect(summary.profilesCount).toBe(1);
    });

    it("suggests adding jobs when profile is ready", () => {
        const action = resolveNextBestAction([profile], [], [], []);
        expect(action.href).toBe("/jobs/new");
    });
});
