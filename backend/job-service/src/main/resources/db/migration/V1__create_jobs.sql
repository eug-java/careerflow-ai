create table job_descriptions (
    id uuid primary key,
    title varchar(255) not null,
    company_name varchar(255),
    location varchar(255),
    employment_type varchar(50),
    salary_min numeric,
    salary_max numeric,
    currency varchar(10),
    remote boolean,
    description text,
    created_at timestamp not null
);

create table job_skills (
    id uuid primary key,
    job_id uuid not null references job_descriptions(id) on delete cascade,
    name varchar(150) not null,
    required boolean
);
