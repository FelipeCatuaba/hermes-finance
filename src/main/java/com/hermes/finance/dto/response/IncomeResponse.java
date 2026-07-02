package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record IncomeResponse(
    UUID id,
    String description,
    BigDecimal amount,
    LocalDate incomeDate,
    UUID categoryId,
    boolean recurring,
    String notes,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
