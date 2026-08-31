import type { ParsedResume } from "../../api/generationApi";
import type { ProfileFormState } from "./ProfileFormFields";

export function createEmptyProfileFormState(): ProfileFormState {
    return {
        fullName: "",
        professionalTitle: "",
        email: "",
        phone: "",
        locationPreference: "CITY",
        location: "Austin, TX",
        summary: "",
        skills: [],
        experiences: [],
    };
}

export function mergeParsedResume(
    current: ProfileFormState,
    parsed: ParsedResume
): ProfileFormState {
    return {
        ...current,
        fullName: parsed.fullName ?? current.fullName,
        professionalTitle: parsed.professionalTitle ?? current.professionalTitle,
        email: parsed.email ?? current.email,
        phone: parsed.phone ?? current.phone,
        locationPreference: parsed.locationPreference ?? current.locationPreference,
        location: parsed.location ?? current.location,
        summary: parsed.summary ?? current.summary,
        skills: parsed.skills ?? current.skills,
        experiences: parsed.experiences ?? current.experiences,
    };
}
