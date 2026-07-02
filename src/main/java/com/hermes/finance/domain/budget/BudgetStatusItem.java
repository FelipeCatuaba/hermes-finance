package com.hermes.finance.domain.budget;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetStatusItem(
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColorHex,
    UUID budgetId,
    BigDecimal amountLimit,
    BigDecimal spentAmount
) {
}
