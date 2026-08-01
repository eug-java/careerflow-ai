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
