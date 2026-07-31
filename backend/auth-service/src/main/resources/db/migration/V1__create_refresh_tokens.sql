CREATE TABLE refresh_tokens (
    token VARCHAR(64) PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    user_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
