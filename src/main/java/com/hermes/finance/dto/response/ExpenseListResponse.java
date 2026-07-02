package com.hermes.finance.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ExpenseListResponse(
    List<ExpenseListItemResponse> items,
    BigDecimal totalAmount,
    long total,
    int page,
    int size,
    int totalPages
) {
}
