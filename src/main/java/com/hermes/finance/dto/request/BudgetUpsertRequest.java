package com.hermes.finance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetUpsertRequest(
    @NotNull UUID categoryId,
    int month,
    int year,
    @NotNull @Positive BigDecimal amountLimit
) {
}
