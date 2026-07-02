package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BudgetResponse(
    UUID id,
    UUID categoryId,
    int month,
    int year,
    BigDecimal amountLimit,
    OffsetDateTime createdAt
) {
}
