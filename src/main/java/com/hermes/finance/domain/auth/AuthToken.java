package com.hermes.finance.domain.auth;

public record AuthToken(
    String value,
    long expiresInSeconds
) {
}
