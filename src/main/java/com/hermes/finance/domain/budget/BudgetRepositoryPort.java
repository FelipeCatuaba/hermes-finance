package com.hermes.finance.domain.budget;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepositoryPort {
    Optional<Budget> findById(UUID id);
    List<Budget> findByUserAndPeriod(UUID userId, int month, int year);
    Budget upsert(Budget budget);
    Optional<Budget> update(Budget budget);
    void delete(UUID id, UUID userId);
    boolean categoryIsAccessible(UUID categoryId, UUID userId);
    List<BudgetStatusItem> findStatus(UUID userId, int month, int year, LocalDate startDate, LocalDate endDate);
    void copyPreviousMonth(UUID userId, int fromMonth, int fromYear, int toMonth, int toYear);
}
