package com.hermes.finance.domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MonthlyIncomeItem(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDate incomeDate,
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    boolean recurring
) {
}
