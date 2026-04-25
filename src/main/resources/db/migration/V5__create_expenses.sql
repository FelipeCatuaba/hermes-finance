CREATE TABLE expenses (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id              UUID         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    family_member_id     UUID REFERENCES family_members(id) ON DELETE SET NULL,
    category_id          UUID REFERENCES expense_categories(id) ON DELETE SET NULL,
    description          VARCHAR(255) NOT NULL,
    amount               NUMERIC(12,2) NOT NULL,
    expense_date         DATE NOT NULL,
    installment_group_id UUID,
    installment_number   SMALLINT,
    total_installments   SMALLINT,
    is_recurring         BOOLEAN      NOT NULL DEFAULT FALSE,
    is_fixed             BOOLEAN      NOT NULL DEFAULT FALSE,
    payment_method       VARCHAR(50),
    notes                TEXT,
    scope                VARCHAR(10)  NOT NULL DEFAULT 'owner' CHECK (scope IN ('owner', 'family')),
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_installment_group FOREIGN KEY (installment_group_id) REFERENCES installment_groups(id)
);

CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_group ON expenses(installment_group_id);
CREATE INDEX idx_expenses_member ON expenses(family_member_id);
