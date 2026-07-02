package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BudgetStatusResponse(
    int month,
    int year,
    List<Item> items
) {
    public record Item(
        ExpenseCategorySummaryResponse category,
        UUID budgetId,
        BigDecimal amountLimit,
        BigDecimal spentAmount,
        BigDecimal pctUsed,
        boolean overBudget
    ) {
    }
}
