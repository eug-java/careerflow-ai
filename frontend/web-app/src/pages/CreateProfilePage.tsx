import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { createProfile } from "../api/profileApi";
import { parseResumeFile } from "../api/generationApi";
import ProfileFormFields, { type ProfileFormState } from "../components/profile/ProfileFormFields";
import { createEmptyProfileFormState } from "../components/profile/profileFormState";
import { useToast } from "../hooks/useToast";

export default function CreateProfilePage() {
    const navigate = useNavigate();
    const { pushToast } = useToast();
    const [form, setForm] = useState<ProfileFormState>(createEmptyProfileFormState());
    const [parsingResume, setParsingResume] = useState(false);
    const [saving, setSaving] = useState(false);

    async function handleParseResume(file: File) {
        setParsingResume(true);
        try {
            const parsed = await parseResumeFile(file);
            setForm((current) => ({
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
            }));
            pushToast("success", "Resume parsed. Review the fields before saving.");
        } catch {
            pushToast("error", "Failed to parse resume. Try another file or fill manually.");
        } finally {
            setParsingResume(false);
        }
    }

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setSaving(true);
        try {
            await createProfile(form);
            pushToast("success", "Profile created");
            navigate("/profiles");
        } catch {
            pushToast("error", "Failed to create profile");
        } finally {
            setSaving(false);
        }
    }

    return (
        <AppLayout>
            <div className="max-w-3xl">
                <h1 className="text-4xl font-bold mb-8">Add Profile</h1>
                <form onSubmit={handleSubmit} className="bg-white rounded-2xl shadow p-6 grid gap-5">
                    <ProfileFormFields
                        value={form}
                        onChange={setForm}
                        onParseResumeFile={handleParseResume}
                        parsingResume={parsingResume}
                    />
                    <div className="flex gap-3">
                        <button
                            type="submit"
                            disabled={saving}
                            className="bg-slate-900 text-white px-5 py-3 rounded-xl hover:bg-slate-700 disabled:opacity-60"
                        >
                            {saving ? "Saving..." : "Save Profile"}
                        </button>
                        <button
                            type="button"
                            onClick={() => navigate("/profiles")}
                            className="bg-white border border-slate-300 px-5 py-3 rounded-xl hover:bg-slate-100"
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </AppLayout>
    );
}
