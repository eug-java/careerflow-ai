import { useEffect, useState } from "react";
import AppLayout from "../layouts/AppLayout";
import { deleteProfile, fetchProfiles } from "../api/profileApi";
import type { Profile } from "../api/profileApi";
import { Link } from "react-router-dom";

export default function ProfilesPage() {
    const [profiles, setProfiles] = useState<Profile[]>([]);

    useEffect(() => {
        fetchProfiles().then(setProfiles);
    }, []);

    async function handleDelete(profileId: string) {
        const confirmed = window.confirm("Delete this profile?");

        if (!confirmed) {
            return;
        }

        await deleteProfile(profileId);

        setProfiles((current) =>
            current.filter((profile) => profile.id !== profileId)
        );
    }

    return (
        <AppLayout>
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-4xl font-bold">Profiles</h1>

                <Link
                    to="/profiles/new"
                    className="bg-slate-900 text-white px-4 py-2 rounded-xl hover:bg-slate-700"
                >
                    Add Profile
                </Link>
            </div>

            <div className="grid gap-6">
                {profiles.map((profile) => (
                    <div key={profile.id} className="bg-white rounded-2xl shadow p-6">
                        <h2 className="text-2xl font-semibold">{profile.fullName}</h2>

                        <p className="text-slate-500">{profile.professionalTitle}</p>

                        <p className="mt-2 text-slate-500">{profile.email}</p>

                        <p className="mt-4">{profile.summary}</p>

                        <Link
                            to={`/profiles/${profile.id}/edit`}
                            className="bg-white border border-slate-300 px-4 py-2 rounded-xl hover:bg-slate-100"
                        >
                            Edit
                        </Link>

                        <button
                            onClick={() => handleDelete(profile.id)}
                            className="mt-5 bg-red-600 text-white px-4 py-2 rounded-xl hover:bg-red-500"
                        >
                            Delete
                        </button>
                    </div>
                ))}
            </div>
        </AppLayout>
    );
}