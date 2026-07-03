package com.hermes.finance.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ShareTokenResponse(
    UUID id,
    UUID familyMemberId,
    String familyMemberName,
    String familyMemberRelation,
    int month,
    int year,
    OffsetDateTime expiresAt,
    OffsetDateTime revokedAt,
    OffsetDateTime createdAt,
    String shareUrl
) {
}
