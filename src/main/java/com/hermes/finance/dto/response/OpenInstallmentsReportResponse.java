package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record OpenInstallmentsReportResponse(
    BigDecimal totalCommitted,
    List<Group> groups
) {
    public record Group(
        UUID id,
        String description,
        BigDecimal totalAmount,
        int paidInstallments,
        int totalInstallments,
        LocalDate nextDueDate,
        BigDecimal futureTotal,
        List<FutureInstallment> futureInstallments
    ) {
    }

    public record FutureInstallment(
        UUID id,
        int installmentNumber,
        BigDecimal amount,
        LocalDate dueDate
    ) {
    }
}
