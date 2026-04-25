CREATE TABLE installment_groups (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    description         VARCHAR(255) NOT NULL,
    total_amount        NUMERIC(12,2) NOT NULL,
    total_installments  SMALLINT     NOT NULL,
    first_due_date      DATE         NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
