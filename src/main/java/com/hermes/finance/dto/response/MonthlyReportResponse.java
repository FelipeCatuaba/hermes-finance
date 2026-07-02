package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record MonthlyReportResponse(
    IncomeSection income,
    OwnerExpensesSection ownerExpenses,
    FamilyExpensesSection familyExpenses,
    Summary summary
) {
    public record IncomeSection(BigDecimal total, List<IncomeItem> items) {
    }

    public record IncomeItem(
        UUID id,
        String description,
        BigDecimal amount,
        LocalDate incomeDate,
        ExpenseCategorySummaryResponse category,
        boolean recurring
    ) {
    }

    public record OwnerExpensesSection(BigDecimal total, List<CategoryBreakdown> byCategory) {
    }

    public record CategoryBreakdown(
        ExpenseCategorySummaryResponse category,
        BigDecimal total,
        BigDecimal pctOfIncome
    ) {
    }

    public record FamilyExpensesSection(BigDecimal total, List<MemberBreakdown> byMember) {
    }

    public record MemberBreakdown(
        ExpenseFamilyMemberSummaryResponse familyMember,
        BigDecimal total
    ) {
    }

    public record Summary(
        BigDecimal totalExpenses,
        BigDecimal ownerBalance,
        BigDecimal savingsRate
    ) {
    }
}
