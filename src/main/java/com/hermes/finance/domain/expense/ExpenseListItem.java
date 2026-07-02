package com.hermes.finance.domain.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseListItem(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDate expenseDate,
    UUID categoryId,
    String categoryName,
    String categoryIcon,
    String categoryColorHex,
    UUID familyMemberId,
    String familyMemberName,
    String familyMemberRelation,
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
