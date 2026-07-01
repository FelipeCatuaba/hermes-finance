package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseResponse(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDate expenseDate,
    UUID categoryId,
    UUID familyMemberId,
    String paymentMethod,
    String notes,
    boolean fixed,
    String scope,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
