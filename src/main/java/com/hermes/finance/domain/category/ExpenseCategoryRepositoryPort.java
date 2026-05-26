package com.hermes.finance.domain.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseCategoryRepositoryPort {
    ExpenseCategory save(ExpenseCategory category);
    List<ExpenseCategory> findVisibleForUser(UUID userId, boolean includeInactive);
    Optional<ExpenseCategory> findById(UUID id);
    ExpenseCategory update(ExpenseCategory category);
    void deleteById(UUID id);
    void deactivate(UUID id);
    boolean isCategoryInUse(UUID id);
}
