CREATE TABLE job_applications (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    profile_id UUID NOT NULL,
    job_id UUID NOT NULL,
    status VARCHAR(32) NOT NULL,
    notes TEXT,
    applied_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_job_applications_owner_profile_job UNIQUE (owner_id, profile_id, job_id)
);

CREATE INDEX idx_job_applications_owner_status ON job_applications (owner_id, status);
