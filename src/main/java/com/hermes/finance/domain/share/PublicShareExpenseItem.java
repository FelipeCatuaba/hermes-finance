package com.hermes.finance.domain.share;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PublicShareExpenseItem(
    String description,
    BigDecimal amount,
    LocalDate expenseDate,
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColorHex,
    String paymentMethod,
    boolean fixed
) {
}
