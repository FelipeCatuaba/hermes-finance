package com.hermes.finance.dto.response;

import java.util.UUID;

public record ExpenseFamilyMemberSummaryResponse(
    UUID id,
    String name,
    String relation
) {
}
