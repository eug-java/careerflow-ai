ALTER TABLE job_match_results
    ADD COLUMN IF NOT EXISTS experience_score NUMERIC(5, 2);

UPDATE job_match_results SET experience_score = 50 WHERE experience_score IS NULL;
