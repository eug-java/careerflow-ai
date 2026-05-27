import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import AppLayout from "../layouts/AppLayout";
import { fetchProfileById, updateProfile } from "../api/profileApi";

export default function EditProfilePage() {
    const navigate = useNavigate();
    const { id } = useParams();

    const [fullName, setFullName] = useState("");
    const [professionalTitle, setProfessionalTitle] = useState("");
    const [email, setEmail] = useState("");
    const [phone, setPhone] = useState("");
    const [location, setLocation] = useState("");
    const [summary, setSummary] = useState("");

    useEffect(() => {
        if (!id) {
            return;
        }

        fetchProfileById(id).then((profile) => {
            setFullName(profile.fullName ?? "");
            setProfessionalTitle(profile.professionalTitle ?? "");
            setEmail(profile.email ?? "");
            setPhone(profile.phone ?? "");
            setLocation(profile.location ?? "");
            setSummary(profile.summary ?? "");
        });
    }, [id]);

    async function handleSubmit(event: FormEvent) {
        event.preventDefault();

        if (!id) {
            return;
        }

        await updateProfile(id, {
            fullName,
            professionalTitle,
            email,
            phone,
            location,
            summary,
        });

        navigate("/profiles");
    }

    return (
        <AppLayout>
            <div className="max-w-3xl">
                <h1 className="text-4xl font-bold mb-8">Edit Profile</h1>

                <form
                    onSubmit={handleSubmit}
                    className="bg-white rounded-2xl shadow p-6 grid gap-5"
                >
                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Full name
            </span>
                        <input
                            value={fullName}
                            onChange={(event) => setFullName(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Professional title
            </span>
                        <input
                            value={professionalTitle}
                            onChange={(event) => setProfessionalTitle(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Email
            </span>
                        <input
                            type="email"
                            value={email}
                            onChange={(event) => setEmail(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Phone
            </span>
                        <input
                            value={phone}
                            onChange={(event) => setPhone(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Location
            </span>
                        <input
                            value={location}
                            onChange={(event) => setLocation(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Resume summary / profile text
            </span>
                        <textarea
                            value={summary}
                            onChange={(event) => setSummary(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2 min-h-48"
                            required
                        />
                    </label>

                    <div className="flex gap-3">
                        <button
                            type="submit"
                            className="bg-slate-900 text-white px-5 py-3 rounded-xl hover:bg-slate-700"
                        >
                            Save Changes
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