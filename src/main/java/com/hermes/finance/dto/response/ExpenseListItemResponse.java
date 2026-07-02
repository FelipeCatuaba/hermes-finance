package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseListItemResponse(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDate expenseDate,
    ExpenseCategorySummaryResponse category,
    ExpenseFamilyMemberSummaryResponse familyMember,
    UUID installmentGroupId,
    Integer installmentNumber,
    Integer totalInstallments,
    String paymentMethod,
    String notes,
    boolean fixed,
    String scope,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
