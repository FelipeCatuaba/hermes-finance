package com.hermes.finance.domain.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record OpenInstallmentReportItem(
    UUID groupId,
    String description,
    BigDecimal totalAmount,
    int totalInstallments,
    UUID installmentId,
    int installmentNumber,
    BigDecimal amount,
    LocalDate dueDate,
    LocalDate nextDueDate,
    BigDecimal futureTotal,
    int futureInstallmentsCount
) {
}
