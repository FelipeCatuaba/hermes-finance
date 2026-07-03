package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PublicShareResponse(
    String familyMemberName,
    String familyMemberRelation,
    int month,
    int year,
    BigDecimal total,
    List<PublicShareExpenseResponse> expenses
) {
}
