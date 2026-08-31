import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import AppLayout from "../layouts/AppLayout";
import { fetchProfileById, updateProfile, type LocationPreference } from "../api/profileApi";
import { parseResumeFile, parseResumeText } from "../api/generationApi";
import ProfileFormFields, { type ProfileFormState } from "../components/profile/ProfileFormFields";
import { createEmptyProfileFormState, mergeParsedResume } from "../components/profile/profileFormState";
import { useToast } from "../hooks/useToast";

export default function EditProfilePage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const { pushToast } = useToast();
    const [form, setForm] = useState<ProfileFormState>(createEmptyProfileFormState());
    const [parsingResume, setParsingResume] = useState(false);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (!id) {
            return;
        }

        fetchProfileById(id)
            .then((profile) => {
                setForm({
                    fullName: profile.fullName ?? "",
                    professionalTitle: profile.professionalTitle ?? "",
                    email: profile.email ?? "",
                    phone: profile.phone ?? "",
                    locationPreference: (profile.locationPreference ?? "CITY") as LocationPreference,
                    location: profile.location ?? "Austin, TX",
                    summary: profile.summary ?? "",
                    skills: profile.skills ?? [],
                    experiences: profile.experiences ?? [],
                });
            })
            .catch(() => pushToast("error", "Failed to load profile"));
    }, [id, pushToast]);

    async function applyParsedResume(source: "file" | "text", input: File | string) {
        setParsingResume(true);
        try {
            const parsed =
                source === "file"
                    ? await parseResumeFile(input as File)
                    : await parseResumeText(input as string);
            setForm((current) => mergeParsedResume(current, parsed));
            pushToast("success", "Resume parsed. Review changes before saving.");
        } catch {
            pushToast("error", "Failed to parse resume");
        } finally {
            setParsingResume(false);
        }
    }

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        if (!id) {
            return;
        }

        setSaving(true);
        try {
            await updateProfile(id, form);
            pushToast("success", "Profile updated");
            navigate("/profiles");
        } catch {
            pushToast("error", "Failed to update profile");
        } finally {
            setSaving(false);
        }
    }

    return (
        <AppLayout>
            <div className="max-w-3xl">
                <h1 className="text-4xl font-bold mb-8">Edit Profile</h1>
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
                            {saving ? "Saving..." : "Save Changes"}
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
