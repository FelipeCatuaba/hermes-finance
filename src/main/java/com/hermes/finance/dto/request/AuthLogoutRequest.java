package com.hermes.finance.dto.request;

public record AuthLogoutRequest(
    String refreshToken
) {
}
