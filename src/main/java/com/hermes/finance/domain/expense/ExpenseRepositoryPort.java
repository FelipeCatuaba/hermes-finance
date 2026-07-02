package com.hermes.finance.domain.expense;

import java.util.UUID;

public interface ExpenseRepositoryPort {
    Expense save(Expense expense);
    UUID createInstallmentGroup(UUID userId, String description, java.math.BigDecimal totalAmount, int totalInstallments, java.time.LocalDate firstDueDate);
    boolean familyMemberBelongsToUser(UUID familyMemberId, UUID userId);
    boolean categoryIsAccessible(UUID categoryId, UUID userId);
}
