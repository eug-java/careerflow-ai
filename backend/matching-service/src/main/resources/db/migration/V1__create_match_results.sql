create table job_match_results (
    id uuid primary key,
    profile_id uuid not null,
    job_id uuid not null,
    total_score numeric(5,2) not null,
    skills_score numeric(5,2) not null,
    location_score numeric(5,2) not null,
    salary_score numeric(5,2) not null,
    explanation text,
    created_at timestamp not null
);
