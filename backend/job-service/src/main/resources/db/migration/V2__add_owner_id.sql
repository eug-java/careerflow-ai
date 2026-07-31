alter table job_descriptions add column if not exists owner_id uuid;
create index if not exists idx_job_descriptions_owner_id on job_descriptions(owner_id);
