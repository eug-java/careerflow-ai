alter table candidate_profiles add column if not exists owner_id uuid;
create index if not exists idx_candidate_profiles_owner_id on candidate_profiles(owner_id);
