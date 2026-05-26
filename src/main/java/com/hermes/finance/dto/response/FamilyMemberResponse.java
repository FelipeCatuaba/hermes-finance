package com.hermes.finance.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FamilyMemberResponse(
    UUID id,
    String name,
    String relation,
    boolean active,
    OffsetDateTime createdAt
) {}
