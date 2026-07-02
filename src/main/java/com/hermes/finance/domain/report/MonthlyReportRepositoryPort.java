package com.hermes.finance.domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MonthlyReportRepositoryPort {
    List<MonthlyIncomeItem> findIncomeItems(UUID userId, LocalDate startDate, LocalDate endDate);
    BigDecimal sumIncome(UUID userId, LocalDate startDate, LocalDate endDate);
    List<MonthlyCategoryExpense> sumOwnerExpensesByCategory(UUID userId, LocalDate startDate, LocalDate endDate);
    BigDecimal sumOwnerExpenses(UUID userId, LocalDate startDate, LocalDate endDate);
    List<MonthlyFamilyExpense> sumFamilyExpensesByMember(UUID userId, LocalDate startDate, LocalDate endDate);
    BigDecimal sumFamilyExpenses(UUID userId, LocalDate startDate, LocalDate endDate);
    List<YearlyReportAmount> sumIncomeByMonth(UUID userId, LocalDate startDate, LocalDate endDate);
    List<YearlyReportAmount> sumOwnerExpensesByMonth(UUID userId, LocalDate startDate, LocalDate endDate);
    List<YearlyReportAmount> sumFamilyExpensesByMonth(UUID userId, LocalDate startDate, LocalDate endDate);
    List<OpenInstallmentReportItem> findOpenInstallmentItems(UUID userId, LocalDate today);
}
