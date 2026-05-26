package com.hermes.finance.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExpenseCategoryResponse(
    UUID id,
    String name,
    String icon,
    String colorHex,
    boolean isDefault,
    boolean active,
    OffsetDateTime createdAt
) {}
