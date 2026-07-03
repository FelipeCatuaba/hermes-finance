package com.hermes.finance.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ShareCreateRequest(
    @NotNull UUID familyMemberId,
    @Min(1) @Max(12) int month,
    @Min(1900) @Max(9999) int year,
    @Min(1) @Max(30) Integer expiresInDays
) {
}
