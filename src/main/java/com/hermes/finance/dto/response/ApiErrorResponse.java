package com.hermes.finance.dto.response;

import java.time.Instant;

public record ApiErrorResponse(
    String error,
    String message,
    Instant timestamp,
    String path
) {
}
