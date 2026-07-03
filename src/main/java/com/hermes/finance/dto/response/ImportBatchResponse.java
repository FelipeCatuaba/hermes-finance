package com.hermes.finance.dto.response;

import java.util.List;

public record ImportBatchResponse(
    int imported,
    int failed,
    List<ImportItemError> errors,
    List<String> monthsAffected
) {
}
