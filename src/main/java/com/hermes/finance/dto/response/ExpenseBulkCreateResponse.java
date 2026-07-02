package com.hermes.finance.dto.response;

import java.util.List;

public record ExpenseBulkCreateResponse(
    int createdCount,
    int failedCount,
    List<ExpenseResponse> created,
    List<ItemError> errors
) {
    public record ItemError(
        int index,
        String field,
        String message
    ) {
    }
}
