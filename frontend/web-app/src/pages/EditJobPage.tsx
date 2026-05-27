import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import AppLayout from "../layouts/AppLayout";
import { fetchJobById, updateJob } from "../api/jobApi";

export default function EditJobPage() {
    const navigate = useNavigate();
    const { id } = useParams();

    const [title, setTitle] = useState("");
    const [companyName, setCompanyName] = useState("");
    const [location, setLocation] = useState("");
    const [employmentType, setEmploymentType] = useState("Full-time");
    const [salaryMin, setSalaryMin] = useState("0");
    const [salaryMax, setSalaryMax] = useState("0");
    const [currency, setCurrency] = useState("USD");
    const [remote, setRemote] = useState(false);
    const [description, setDescription] = useState("");
    const [skillsText, setSkillsText] = useState("");

    useEffect(() => {
        if (!id) {
            return;
        }

        fetchJobById(id).then((job) => {
            setTitle(job.title ?? "");
            setCompanyName(job.companyName ?? "");
            setLocation(job.location ?? "");
            setEmploymentType(job.employmentType ?? "Full-time");
            setSalaryMin(String(job.salaryMin ?? 0));
            setSalaryMax(String(job.salaryMax ?? 0));
            setCurrency(job.currency ?? "USD");
            setRemote(Boolean(job.remote));
            setDescription(job.description ?? "");

            const skills = job.skills ?? [];
            setSkillsText(skills.map((skill) => skill.name).join(", "));
        });
    }, [id]);

    async function handleSubmit(event: FormEvent) {
        event.preventDefault();

        if (!id) {
            return;
        }

        const skills = skillsText
            .split(",")
            .map((skill) => skill.trim())
            .filter(Boolean)
            .map((skill) => ({
                name: skill,
                required: true,
            }));

        await updateJob(id, {
            title,
            companyName,
            location,
            employmentType,
            salaryMin: Number(salaryMin),
            salaryMax: Number(salaryMax),
            currency,
            remote,
            description,
            skills,
        });

        navigate("/jobs");
    }

    return (
        <AppLayout>
            <div className="max-w-3xl">
                <h1 className="text-4xl font-bold mb-8">Edit Job</h1>

                <form
                    onSubmit={handleSubmit}
                    className="bg-white rounded-2xl shadow p-6 grid gap-5"
                >
                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Job title
            </span>
                        <input
                            value={title}
                            onChange={(event) => setTitle(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Company name
            </span>
                        <input
                            value={companyName}
                            onChange={(event) => setCompanyName(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            required
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

                    <div className="grid grid-cols-2 gap-4">
                        <label>
              <span className="block text-sm font-medium text-slate-700 mb-1">
                Employment type
              </span>
                            <input
                                value={employmentType}
                                onChange={(event) => setEmploymentType(event.target.value)}
                                className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            />
                        </label>

                        <label>
              <span className="block text-sm font-medium text-slate-700 mb-1">
                Currency
              </span>
                            <input
                                value={currency}
                                onChange={(event) => setCurrency(event.target.value)}
                                className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            />
                        </label>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <label>
              <span className="block text-sm font-medium text-slate-700 mb-1">
                Salary min
              </span>
                            <input
                                type="number"
                                value={salaryMin}
                                onChange={(event) => setSalaryMin(event.target.value)}
                                className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            />
                        </label>

                        <label>
              <span className="block text-sm font-medium text-slate-700 mb-1">
                Salary max
              </span>
                            <input
                                type="number"
                                value={salaryMax}
                                onChange={(event) => setSalaryMax(event.target.value)}
                                className="w-full border border-slate-300 rounded-xl px-4 py-2"
                            />
                        </label>
                    </div>

                    <label className="flex items-center gap-3">
                        <input
                            type="checkbox"
                            checked={remote}
                            onChange={(event) => setRemote(event.target.checked)}
                        />
                        <span className="text-sm text-slate-700">Remote position</span>
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Required skills, comma separated
            </span>
                        <input
                            value={skillsText}
                            onChange={(event) => setSkillsText(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2"
                        />
                    </label>

                    <label>
            <span className="block text-sm font-medium text-slate-700 mb-1">
              Job description
            </span>
                        <textarea
                            value={description}
                            onChange={(event) => setDescription(event.target.value)}
                            className="w-full border border-slate-300 rounded-xl px-4 py-2 min-h-56"
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
                            onClick={() => navigate("/jobs")}
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