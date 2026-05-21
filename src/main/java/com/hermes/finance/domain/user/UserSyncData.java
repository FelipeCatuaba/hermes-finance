package com.hermes.finance.domain.user;

public record UserSyncData(
    String externalAuthId,
    String email,
    String fullName
) {
}
