package com.hermes.finance.dto.request;

import jakarta.validation.constraints.Size;

public record FamilyMemberUpsertRequest(
    @Size(min = 1, max = 100, message = "name must be between 1 and 100 characters")
    String name,
    @Size(max = 100, message = "relation must be at most 100 characters")
    String relation
) {}
