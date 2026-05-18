package com.hermes.finance.dto.response;

public record AuthTokensResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {
}
