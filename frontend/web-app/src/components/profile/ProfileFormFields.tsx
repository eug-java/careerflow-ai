import { useMemo, useState } from "react";
import type { Experience, Skill, LocationPreference } from "../../api/profileApi";
import {
    NATIONWIDE_OPTION,
    locationOptionsForPreference,
} from "../../lib/locationOptions";

export interface ProfileFormState {
    fullName: string;
    professionalTitle: string;
    email: string;
    phone: string;
    locationPreference: LocationPreference;
    location: string;
    summary: string;
    skills: Skill[];
    experiences: Experience[];
}

interface Props {
    value: ProfileFormState;
    onChange: (next: ProfileFormState) => void;
    onParseResumeFile?: (file: File) => Promise<void>;
    onParseResumeText?: (text: string) => Promise<void>;
    parsingResume?: boolean;
}

const emptySkill = (): Skill => ({ name: "", category: "", yearsOfExperience: 1 });
const emptyExperience = (): Experience => ({
    companyName: "",
    positionTitle: "",
    location: "",
    startDate: "",
    endDate: "",
    currentPosition: false,
    description: "",
});

export default function ProfileFormFields({
    value,
    onChange,
    onParseResumeFile,
    onParseResumeText,
    parsingResume = false,
}: Props) {
    const [skillDraft, setSkillDraft] = useState(emptySkill());
    const [resumeText, setResumeText] = useState("");

    const locationOptions = useMemo(
        () => locationOptionsForPreference(value.locationPreference),
        [value.locationPreference]
    );

    function update(partial: Partial<ProfileFormState>) {
        onChange({ ...value, ...partial });
    }

    function handlePreferenceChange(preference: LocationPreference) {
        const options = locationOptionsForPreference(preference);
        update({
            locationPreference: preference,
            location: options[0]?.value ?? "",
        });
    }

    return (
        <div className="grid gap-5">
            {(onParseResumeFile || onParseResumeText) && (
                <div className="rounded-xl border border-dashed border-slate-300 p-4 bg-slate-50 grid gap-4">
                    {onParseResumeFile && (
                        <label className="block">
                            <span className="block text-sm font-medium text-slate-700 mb-2">
                                Upload control resume (PDF, DOCX, TXT)
                            </span>
                            <input
                                type="file"
                                accept=".pdf,.doc,.docx,.txt,.md"
                                disabled={parsingResume}
                                onChange={(event) => {
                                    const file = event.target.files?.[0];
                                    if (file) {
                                        void onParseResumeFile(file);
                                    }
                                }}
                                className="block w-full text-sm"
                            />
                        </label>
                    )}

                    {onParseResumeText && (
                        <div>
                            <span className="block text-sm font-medium text-slate-700 mb-2">
                                Or paste resume text
                            </span>
                            <textarea
                                value={resumeText}
                                onChange={(event) => setResumeText(event.target.value)}
                                disabled={parsingResume}
                                className="w-full border border-slate-300 rounded-xl px-4 py-2 min-h-40"
                                placeholder="Paste resume content here..."
                            />
                            <button
                                type="button"
                                disabled={parsingResume || !resumeText.trim()}
                                onClick={() => void onParseResumeText(resumeText)}
                                className="mt-3 bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700 disabled:opacity-50 text-sm"
                            >
                                {parsingResume ? "Parsing..." : "Parse resume text"}
                            </button>
                        </div>
                    )}

                    <p className="text-xs text-slate-500">
                        We parse location, skills, and experience to pre-fill your profile.
                    </p>
                </div>
            )}

            <label>
                <span className="block text-sm font-medium text-slate-700 mb-1">Full name</span>
                <input
                    value={value.fullName}
                    onChange={(event) => update({ fullName: event.target.value })}
                    className="w-full border border-slate-300 rounded-xl px-4 py-2"
                    required
                />
            </label>

            <label>
                <span className="block text-sm font-medium text-slate-700 mb-1">Professional title</span>
                <input
                    value={value.professionalTitle}
                    onChange={(event) => update({ professionalTitle: event.target.value })}
                    className="w-full border border-slate-300 rounded-xl px-4 py-2"
                    required
                />
            </label>

            <div className="grid md:grid-cols-2 gap-4">
                <label>
                    <span className="block text-sm font-medium text-slate-700 mb-1">Email</span>
                    <input
                        type="email"
                        value={value.email}
                        onChange={(event) => update({ email: event.target.value })}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                        required
                    />
                </label>
                <label>
                    <span className="block text-sm font-medium text-slate-700 mb-1">Phone</span>
                    <input
                        value={value.phone}
                        onChange={(event) => update({ phone: event.target.value })}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                    />
                </label>
            </div>

            <div className="grid md:grid-cols-2 gap-4">
                <label>
                    <span className="block text-sm font-medium text-slate-700 mb-1">Location type</span>
                    <select
                        value={value.locationPreference}
                        onChange={(event) =>
                            handlePreferenceChange(event.target.value as LocationPreference)
                        }
                        className="w-full border border-slate-300 rounded-xl px-4 py-2"
                    >
                        <option value="CITY">US City</option>
                        <option value="METRO">Metro area</option>
                        <option value="NATIONWIDE">Nationwide relocation</option>
                    </select>
                </label>
                <label>
                    <span className="block text-sm font-medium text-slate-700 mb-1">Location</span>
                    {value.locationPreference === "NATIONWIDE" ? (
                        <input
                            value={NATIONWIDE_OPTION.value}
                            readOnly
                            className="w-full border border-slate-200 rounded-xl px-4 py-2 bg-slate-50"
                        />
                    ) : (
                        <select
                            value={value.location}
                            onChange={(event) => update({ location: event.target.value })}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
                        >
                            {locationOptions.map((option) => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    )}
                </label>
            </div>

            <label>
                <span className="block text-sm font-medium text-slate-700 mb-1">Resume summary</span>
                <textarea
                    value={value.summary}
                    onChange={(event) => update({ summary: event.target.value })}
                    className="w-full border border-slate-300 rounded-xl px-4 py-2 min-h-40"
                    required
                />
            </label>

            <section className="rounded-xl border border-slate-200 p-4">
                <h2 className="font-semibold mb-3">Skills</h2>
                <div className="grid md:grid-cols-4 gap-2 mb-3">
                    <input
                        placeholder="Skill name"
                        value={skillDraft.name}
                        onChange={(event) => setSkillDraft({ ...skillDraft, name: event.target.value })}
                        className="border border-slate-300 rounded-xl px-3 py-2 md:col-span-2"
                    />
                    <input
                        placeholder="Category"
                        value={skillDraft.category ?? ""}
                        onChange={(event) => setSkillDraft({ ...skillDraft, category: event.target.value })}
                        className="border border-slate-300 rounded-xl px-3 py-2"
                    />
                    <input
                        type="number"
                        min={0}
                        step={0.5}
                        placeholder="Years"
                        value={skillDraft.yearsOfExperience ?? 1}
                        onChange={(event) =>
                            setSkillDraft({
                                ...skillDraft,
                                yearsOfExperience: Number(event.target.value),
                            })
                        }
                        className="border border-slate-300 rounded-xl px-3 py-2"
                    />
                </div>
                <button
                    type="button"
                    onClick={() => {
                        if (!skillDraft.name.trim()) {
                            return;
                        }
                        update({ skills: [...value.skills, { ...skillDraft, name: skillDraft.name.trim() }] });
                        setSkillDraft(emptySkill());
                    }}
                    className="text-sm bg-slate-900 text-white px-3 py-2 rounded-lg"
                >
                    Add skill
                </button>
                <ul className="mt-3 space-y-2">
                    {value.skills.map((skill, index) => (
                        <li key={`${skill.name}-${index}`} className="flex justify-between text-sm bg-slate-50 rounded-lg px-3 py-2">
                            <span>
                                {skill.name}
                                {skill.category ? ` · ${skill.category}` : ""}
                                {skill.yearsOfExperience != null ? ` · ${skill.yearsOfExperience}y` : ""}
                            </span>
                            <button
                                type="button"
                                onClick={() =>
                                    update({ skills: value.skills.filter((_, i) => i !== index) })
                                }
                                className="text-red-600"
                            >
                                Remove
                            </button>
                        </li>
                    ))}
                </ul>
            </section>

            <section className="rounded-xl border border-slate-200 p-4">
                <div className="flex items-center justify-between mb-3">
                    <h2 className="font-semibold">Experience</h2>
                    <button
                        type="button"
                        onClick={() => update({ experiences: [...value.experiences, emptyExperience()] })}
                        className="text-sm bg-slate-100 px-3 py-2 rounded-lg"
                    >
                        Add experience
                    </button>
                </div>
                <div className="space-y-4">
                    {value.experiences.map((experience, index) => (
                        <div key={index} className="grid gap-2 border border-slate-100 rounded-lg p-3">
                            <div className="grid md:grid-cols-2 gap-2">
                                <input
                                    placeholder="Company"
                                    value={experience.companyName}
                                    onChange={(event) => {
                                        const experiences = [...value.experiences];
                                        experiences[index] = { ...experience, companyName: event.target.value };
                                        update({ experiences });
                                    }}
                                    className="border border-slate-300 rounded-xl px-3 py-2"
                                />
                                <input
                                    placeholder="Position"
                                    value={experience.positionTitle}
                                    onChange={(event) => {
                                        const experiences = [...value.experiences];
                                        experiences[index] = { ...experience, positionTitle: event.target.value };
                                        update({ experiences });
                                    }}
                                    className="border border-slate-300 rounded-xl px-3 py-2"
                                />
                            </div>
                            <textarea
                                placeholder="Description"
                                value={experience.description ?? ""}
                                onChange={(event) => {
                                    const experiences = [...value.experiences];
                                    experiences[index] = { ...experience, description: event.target.value };
                                    update({ experiences });
                                }}
                                className="border border-slate-300 rounded-xl px-3 py-2 min-h-20"
                            />
                            <button
                                type="button"
                                onClick={() =>
                                    update({ experiences: value.experiences.filter((_, i) => i !== index) })
                                }
                                className="text-sm text-red-600 text-left"
                            >
                                Remove experience
                            </button>
                        </div>
                    ))}
                </div>
            </section>
        </div>
    );
}
