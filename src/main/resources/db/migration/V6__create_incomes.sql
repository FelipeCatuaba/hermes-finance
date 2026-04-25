CREATE TABLE incomes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     UUID REFERENCES expense_categories(id) ON DELETE SET NULL,
    description     VARCHAR(255) NOT NULL,
    amount          NUMERIC(12,2) NOT NULL CHECK (amount > 0),
    income_date     DATE NOT NULL,
    is_recurring    BOOLEAN NOT NULL DEFAULT FALSE,
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_incomes_user_date ON incomes(user_id, income_date);
