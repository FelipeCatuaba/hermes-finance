package com.hermes.finance.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ImportExpenseRequest(
    Integer index,
    String description,
    BigDecimal amount,
    LocalDate expenseDate,
    UUID categoryId,
    UUID familyMemberId,
    String paymentMethod,
    String notes,
    Boolean isFixed,
    Integer totalInstallments,
    LocalDate firstDueDate
) {
}
