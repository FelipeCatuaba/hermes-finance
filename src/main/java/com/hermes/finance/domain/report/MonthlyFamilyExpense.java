package com.hermes.finance.domain.report;

import java.math.BigDecimal;
import java.util.UUID;

public record MonthlyFamilyExpense(
    UUID familyMemberId,
    String familyMemberName,
    String familyMemberRelation,
    BigDecimal total
) {
}
