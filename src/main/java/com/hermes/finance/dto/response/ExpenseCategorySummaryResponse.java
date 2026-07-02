package com.hermes.finance.dto.response;

import java.util.UUID;

public record ExpenseCategorySummaryResponse(
    UUID id,
    String name,
    String icon,
    String colorHex
) {
}
