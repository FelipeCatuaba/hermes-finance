package com.hermes.finance.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExpenseCategoryUpsertRequest(
    @Size(min = 1, max = 100, message = "name must be between 1 and 100 characters")
    String name,
    @Size(max = 50, message = "icon must be at most 50 characters")
    String icon,
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "colorHex must be a valid hex color")
    String colorHex
) {}
