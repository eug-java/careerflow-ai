import type { FormEvent } from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import AppLayout from "../layouts/AppLayout";
import { createJob, parseJobDescriptionWithAi } from "../api/jobApi";

export default function CreateJobPage() {
    const navigate = useNavigate();

    const [title, setTitle] = useState("");
    const [companyName, setCompanyName] = useState("");
    const [location, setLocation] = useState("");
    const [employmentType, setEmploymentType] = useState("Full-time");
    const [salaryMin, setSalaryMin] = useState("120000");
    const [salaryMax, setSalaryMax] = useState("150000");
    const [currency, setCurrency] = useState("USD");
    const [remote, setRemote] = useState(false);
    const [description, setDescription] = useState("");
    const [skillsText, setSkillsText] = useState("Java, Spring Boot, Kafka, PostgreSQL");

    const [rawJobDescription, setRawJobDescription] = useState("");
    const [isParsing, setIsParsing] = useState(false);
    const [parseMessage, setParseMessage] = useState("");

    async function handleParseWithAi() {
        if (!rawJobDescription.trim()) {
            setParseMessage("Paste job description first.");
            return;
        }

        setIsParsing(true);
        setParseMessage("");

        try {
            const parsed = await parseJobDescriptionWithAi({
                text: rawJobDescription,
            });

            setTitle(parsed.title ?? "");
            setCompanyName(parsed.companyName ?? "Unknown Company");
            setLocation(parsed.location ?? "Unknown Location");
            setEmploymentType(parsed.employmentType ?? "Full-time");
            setSalaryMin(parsed.salaryMin == null ? "" : String(parsed.salaryMin));
            setSalaryMax(parsed.salaryMax == null ? "" : String(parsed.salaryMax));
            setCurrency(parsed.currency ?? "USD");
            setRemote(Boolean(parsed.remote));
            setDescription(parsed.description ?? rawJobDescription);

            setSkillsText(
                parsed.skills?.map((skill) => skill.name).join(", ") ?? ""
            );

            setParseMessage("Job description parsed successfully.");
        } catch (error) {
            console.error(error);
            setParseMessage("Failed to parse job description.");
        } finally {
            setIsParsing(false);
        }
    }

    async function handleSubmit(event: FormEvent) {
        event.preventDefault();

        const skills = skillsText
            .split(",")
            .map((skill) => skill.trim())
            .filter(Boolean)
            .map((skill) => ({
                name: skill,
                required: true,
            }));

        await createJob({
            title,
            companyName,
            location,
            employmentType,
            salaryMin: Number(salaryMin || 0),
            salaryMax: Number(salaryMax || 0),
            currency,
            remote,
            description,
            skills,
        });

        navigate("/jobs");
    }

    return (
        <AppLayout>
            <div className="max-w-4xl">
                <h1 className="text-4xl font-bold mb-8">Add Job</h1>

                <div className="bg-white rounded-2xl shadow p-6 mb-6">
                    <h2 className="text-2xl font-semibold mb-2">
                        AI job parser
                    </h2>

                    <p className="text-slate-500 mb-4">
                        Paste a raw job description and let AI fill the form.
                    </p>

                    <textarea
                        value={rawJobDescription}
                        onChange={(event) => setRawJobDescription(event.target.value)}
                        className="w-full border border-slate-300 rounded-xl px-4 py-2 min-h-56"
                        placeholder="Paste full job description here..."
                    />

                    <div className="flex items-center gap-3 mt-4">
                        <button
                            type="button"
                            onClick={handleParseWithAi}
                            disabled={isParsing}
                            className="bg-slate-900 text-white px-5 py-3 rounded-xl hover:bg-slate-700 disabled:opacity-50"
                        >
                            {isParsing ? "Parsing..." : "Parse with AI"}
                        </button>

                        {parseMessage && (
                            <span className="text-sm text-slate-600">
                {parseMessage}
              </span>
                        )}
                    </div>
                </div>

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
                            Save Job
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