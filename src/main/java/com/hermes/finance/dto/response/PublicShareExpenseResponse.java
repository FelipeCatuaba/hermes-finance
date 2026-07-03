package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PublicShareExpenseResponse(
    String description,
    BigDecimal amount,
    LocalDate expenseDate,
    ExpenseCategorySummaryResponse category,
    String paymentMethod,
    boolean fixed
) {
}
