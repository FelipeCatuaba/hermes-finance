package com.hermes.finance.domain.webhook;

import com.hermes.finance.domain.user.UserSyncData;
import com.hermes.finance.dto.request.ClerkWebhookEvent;
import org.springframework.stereotype.Component;

@Component
public class ClerkWebhookEventMapper {

    public UserSyncData toUserSyncData(ClerkWebhookEvent.Data data) {
        if (data == null) {
            return null;
        }
        return new UserSyncData(data.getId(), data.getPrimaryEmail(), data.getFullName());
    }
}
