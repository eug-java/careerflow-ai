export type LocationPreference = "CITY" | "METRO" | "NATIONWIDE";

export interface LocationOption {
    value: string;
    label: string;
    preference: LocationPreference;
}

export const US_CITIES: LocationOption[] = [
    { value: "Austin, TX", label: "Austin, TX", preference: "CITY" },
    { value: "Dallas, TX", label: "Dallas, TX", preference: "CITY" },
    { value: "Houston, TX", label: "Houston, TX", preference: "CITY" },
    { value: "San Francisco, CA", label: "San Francisco, CA", preference: "CITY" },
    { value: "San Jose, CA", label: "San Jose, CA", preference: "CITY" },
    { value: "Los Angeles, CA", label: "Los Angeles, CA", preference: "CITY" },
    { value: "Seattle, WA", label: "Seattle, WA", preference: "CITY" },
    { value: "New York, NY", label: "New York, NY", preference: "CITY" },
    { value: "Chicago, IL", label: "Chicago, IL", preference: "CITY" },
    { value: "Boston, MA", label: "Boston, MA", preference: "CITY" },
    { value: "Denver, CO", label: "Denver, CO", preference: "CITY" },
    { value: "Atlanta, GA", label: "Atlanta, GA", preference: "CITY" },
    { value: "Miami, FL", label: "Miami, FL", preference: "CITY" },
    { value: "Phoenix, AZ", label: "Phoenix, AZ", preference: "CITY" },
    { value: "Portland, OR", label: "Portland, OR", preference: "CITY" },
    { value: "Washington, DC", label: "Washington, DC", preference: "CITY" },
];

export const US_METROS: LocationOption[] = [
    { value: "San Francisco Bay Area", label: "San Francisco Bay Area", preference: "METRO" },
    { value: "New York Metro", label: "New York Metro", preference: "METRO" },
    { value: "Los Angeles Metro", label: "Los Angeles Metro", preference: "METRO" },
    { value: "Dallas-Fort Worth", label: "Dallas-Fort Worth", preference: "METRO" },
    { value: "Austin Metro", label: "Austin Metro", preference: "METRO" },
    { value: "Seattle Metro", label: "Seattle Metro", preference: "METRO" },
    { value: "Chicago Metro", label: "Chicago Metro", preference: "METRO" },
    { value: "Boston Metro", label: "Boston Metro", preference: "METRO" },
    { value: "Denver Metro", label: "Denver Metro", preference: "METRO" },
    { value: "Atlanta Metro", label: "Atlanta Metro", preference: "METRO" },
    { value: "Miami Metro", label: "Miami Metro", preference: "METRO" },
    { value: "Washington DC Metro", label: "Washington DC Metro", preference: "METRO" },
];

export const NATIONWIDE_OPTION: LocationOption = {
    value: "Open to relocation anywhere in USA",
    label: "Relocation anywhere in USA",
    preference: "NATIONWIDE",
};

export function locationOptionsForPreference(preference: LocationPreference): LocationOption[] {
    if (preference === "METRO") {
        return US_METROS;
    }
    if (preference === "NATIONWIDE") {
        return [NATIONWIDE_OPTION];
    }
    return US_CITIES;
}
