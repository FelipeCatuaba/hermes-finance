package com.hermes.finance.domain.report;

import java.math.BigDecimal;

public record YearlyReportAmount(
    int month,
    BigDecimal total
) {
}
