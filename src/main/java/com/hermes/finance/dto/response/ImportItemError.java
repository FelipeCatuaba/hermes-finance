package com.hermes.finance.dto.response;

public record ImportItemError(
    int index,
    String field,
    String message
) {
}
