package com.hermes.finance.domain.webhook;

import com.hermes.finance.domain.identity.IdentityEventType;
import com.hermes.finance.domain.identity.IdentityUserEvent;
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

    public IdentityUserEvent toIdentityUserEvent(ClerkWebhookEvent event) {
        if (event == null) {
            return new IdentityUserEvent(IdentityEventType.UNSUPPORTED, null);
        }

        UserSyncData data = toUserSyncData(event.getData());
        String type = event.getType() != null ? event.getType() : "";

        return switch (type) {
            case "user.created" -> new IdentityUserEvent(IdentityEventType.USER_CREATED, data);
            case "user.updated" -> new IdentityUserEvent(IdentityEventType.USER_UPDATED, data);
            case "user.deleted" -> new IdentityUserEvent(IdentityEventType.USER_DELETED, data);
            default -> new IdentityUserEvent(IdentityEventType.UNSUPPORTED, data);
        };
    }
}
