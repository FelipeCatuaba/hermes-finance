package com.hermes.finance.domain.income;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncomeRepositoryPort {
    List<Income> findByUserAndPeriod(UUID userId, LocalDate startDate, LocalDate endDate);
    Optional<Income> findById(UUID id);
    Income save(Income income);
    Optional<Income> update(Income income);
    void delete(UUID id, UUID userId);
    boolean categoryIsAccessible(UUID categoryId, UUID userId);
}
