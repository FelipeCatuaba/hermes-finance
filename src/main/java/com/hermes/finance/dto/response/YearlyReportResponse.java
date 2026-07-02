package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record YearlyReportResponse(
    int year,
    List<MonthSummary> months
) {
    public record MonthSummary(
        int month,
        BigDecimal incomeTotal,
        BigDecimal ownerExpensesTotal,
        BigDecimal familyExpensesTotal,
        BigDecimal savingsRate
    ) {
    }
}
