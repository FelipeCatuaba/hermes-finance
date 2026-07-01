package com.hermes.finance.domain.expense;

import java.util.UUID;

public interface ExpenseRepositoryPort {
    Expense save(Expense expense);
    boolean familyMemberBelongsToUser(UUID familyMemberId, UUID userId);
    boolean categoryIsAccessible(UUID categoryId, UUID userId);
}
