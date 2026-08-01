ALTER TABLE candidate_profiles
    ADD COLUMN IF NOT EXISTS location_preference VARCHAR(32) NOT NULL DEFAULT 'CITY';
