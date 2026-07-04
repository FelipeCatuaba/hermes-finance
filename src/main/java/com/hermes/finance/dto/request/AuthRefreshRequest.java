package com.hermes.finance.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AuthRefreshRequest(
    @NotBlank String refreshToken
) {
}
