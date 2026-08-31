import { describe, expect, it } from "vitest";
import { createEmptyProfileFormState, mergeParsedResume } from "./profileFormState";

describe("mergeParsedResume", () => {
    it("merges parsed fields without dropping existing values", () => {
        const current = {
            ...createEmptyProfileFormState(),
            fullName: "Existing Name",
            email: "keep@example.com",
        };

        const merged = mergeParsedResume(current, {
            fullName: "Parsed Name",
            professionalTitle: "Engineer",
            skills: [{ name: "Java", category: "Backend", yearsOfExperience: 5 }],
        });

        expect(merged.fullName).toBe("Parsed Name");
        expect(merged.professionalTitle).toBe("Engineer");
        expect(merged.email).toBe("keep@example.com");
        expect(merged.skills).toHaveLength(1);
    });
});
