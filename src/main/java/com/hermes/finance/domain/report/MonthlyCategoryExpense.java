package com.hermes.finance.domain.report;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyCategoryExpense(
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColorHex,
    BigDecimal total
) {
}
