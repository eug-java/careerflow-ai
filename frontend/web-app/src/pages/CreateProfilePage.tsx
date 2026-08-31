import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { createProfile } from "../api/profileApi";
import { parseResumeFile, parseResumeText } from "../api/generationApi";
import ProfileFormFields, { type ProfileFormState } from "../components/profile/ProfileFormFields";
import { createEmptyProfileFormState, mergeParsedResume } from "../components/profile/profileFormState";
import { useToast } from "../hooks/useToast";

export default function CreateProfilePage() {
    const navigate = useNavigate();
    const { pushToast } = useToast();
    const [form, setForm] = useState<ProfileFormState>(createEmptyProfileFormState());
    const [parsingResume, setParsingResume] = useState(false);
    const [saving, setSaving] = useState(false);

    async function applyParsedResume(source: "file" | "text", input: File | string) {
        setParsingResume(true);
        try {
            const parsed =
                source === "file"
                    ? await parseResumeFile(input as File)
                    : await parseResumeText(input as string);
            setForm((current) => mergeParsedResume(current, parsed));
            pushToast("success", "Resume parsed. Review the fields before saving.");
        } catch {
            pushToast("error", "Failed to parse resume. Try another input or fill manually.");
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
                        onParseResumeFile={(file) => applyParsedResume("file", file)}
                        onParseResumeText={(text) => applyParsedResume("text", text)}
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
