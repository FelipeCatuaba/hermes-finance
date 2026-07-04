package com.hermes.finance.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresInSeconds,
    AuthUserResponse user
) {
}
