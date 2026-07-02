package com.hermes.finance.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record IncomeUpsertRequest(
    String description,
    BigDecimal amount,
    LocalDate incomeDate,
    UUID categoryId,
    Boolean isRecurring,
    String notes
) {
}
