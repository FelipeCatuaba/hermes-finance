package com.hermes.finance.domain.expense;

import java.util.UUID;

public interface ExpenseRepositoryPort {
    java.util.Optional<Expense> findById(UUID id);
    Expense save(Expense expense);
    java.util.Optional<Expense> update(Expense expense);
    void delete(UUID id, UUID userId);
    UUID createInstallmentGroup(UUID userId, String description, java.math.BigDecimal totalAmount, int totalInstallments, java.time.LocalDate firstDueDate);
    java.util.Optional<UUID> findInstallmentGroupUserId(UUID id);
    void deleteExpensesByInstallmentGroup(UUID id, UUID userId);
    void deleteInstallmentGroup(UUID id, UUID userId);
    boolean familyMemberBelongsToUser(UUID familyMemberId, UUID userId);
    boolean categoryIsAccessible(UUID categoryId, UUID userId);
}
