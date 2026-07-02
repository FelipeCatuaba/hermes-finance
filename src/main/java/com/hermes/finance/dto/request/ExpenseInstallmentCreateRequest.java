package com.hermes.finance.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseInstallmentCreateRequest(
    String description,
    BigDecimal totalAmount,
    Integer totalInstallments,
    LocalDate firstDueDate,
    UUID categoryId,
    UUID familyMemberId,
    String paymentMethod
) {
}
