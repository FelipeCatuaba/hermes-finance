CREATE TABLE share_tokens (
    id               UUID PRIMARY KEY,
    user_id          UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    family_member_id UUID NOT NULL REFERENCES family_members(id) ON DELETE CASCADE,
    month            INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    year             INTEGER NOT NULL CHECK (year BETWEEN 1900 AND 9999),
    token_hash       VARCHAR(64) NOT NULL UNIQUE,
    expires_at       TIMESTAMPTZ NOT NULL,
    revoked_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_share_tokens_user_period ON share_tokens(user_id, family_member_id, month, year);
CREATE INDEX idx_share_tokens_hash_active ON share_tokens(token_hash) WHERE revoked_at IS NULL;
