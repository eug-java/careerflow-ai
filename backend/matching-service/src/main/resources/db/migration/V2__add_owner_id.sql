alter table job_match_results add column if not exists owner_id uuid;
create index if not exists idx_job_match_results_owner_id on job_match_results(owner_id);
