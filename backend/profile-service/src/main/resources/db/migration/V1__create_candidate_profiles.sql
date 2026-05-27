create table candidate_profiles (
    id uuid primary key,
    full_name varchar(255) not null,
    professional_title varchar(255),
    email varchar(255),
    phone varchar(50),
    location varchar(255),
    summary text,
    created_at timestamp not null,
    updated_at timestamp not null
);

create table candidate_skills (
    id uuid primary key,
    profile_id uuid not null references candidate_profiles(id) on delete cascade,
    name varchar(150) not null,
    category varchar(100),
    years_of_experience numeric(4,1)
);

create table candidate_experiences (
    id uuid primary key,
    profile_id uuid not null references candidate_profiles(id) on delete cascade,
    company_name varchar(255) not null,
    position_title varchar(255) not null,
    location varchar(255),
    start_date date,
    end_date date,
    current_position boolean not null default false,
    description text
);
