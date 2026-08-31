CREATE TABLE oauth_identities (
    id UUID PRIMARY KEY,
    provider VARCHAR(32) NOT NULL,
    provider_subject VARCHAR(128) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_oauth_identities_provider_subject UNIQUE (provider, provider_subject)
);

CREATE INDEX idx_oauth_identities_user_id ON oauth_identities (user_id);
