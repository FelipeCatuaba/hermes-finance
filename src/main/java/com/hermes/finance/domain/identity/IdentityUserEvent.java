package com.hermes.finance.domain.identity;

import com.hermes.finance.domain.user.UserSyncData;

public record IdentityUserEvent(
    IdentityEventType type,
    UserSyncData data
) {
}

