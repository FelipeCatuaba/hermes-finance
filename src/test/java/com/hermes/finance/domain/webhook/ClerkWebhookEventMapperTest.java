package com.hermes.finance.domain.webhook;

import com.hermes.finance.domain.identity.IdentityEventType;
import com.hermes.finance.domain.identity.IdentityUserEvent;
import com.hermes.finance.dto.request.ClerkWebhookEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClerkWebhookEventMapperTest {

    private final ClerkWebhookEventMapper mapper = new ClerkWebhookEventMapper();

    @Test
    void shouldReturnNullWhenDataIsNull() {
        assertNull(mapper.toUserSyncData(null));
    }

    @Test
    void shouldMapUserSyncData() {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("u1");
        data.setFirstName("Felipe");
        data.setLastName("Catuaba");

        ClerkWebhookEvent.EmailAddress email = new ClerkWebhookEvent.EmailAddress();
        email.setPrimary(true);
        email.setEmailAddress("f@h.com");
        data.setEmailAddresses(java.util.List.of(email));

        assertEquals("u1", mapper.toUserSyncData(data).externalAuthId());
        assertEquals("f@h.com", mapper.toUserSyncData(data).email());
        assertEquals("Felipe Catuaba", mapper.toUserSyncData(data).fullName());
    }

    @Test
    void shouldReturnUnsupportedWhenEventIsNull() {
        IdentityUserEvent event = mapper.toIdentityUserEvent(null);
        assertEquals(IdentityEventType.UNSUPPORTED, event.type());
        assertNull(event.data());
    }

    @Test
    void shouldMapKnownEventTypes() {
        assertType("user.created", IdentityEventType.USER_CREATED);
        assertType("user.updated", IdentityEventType.USER_UPDATED);
        assertType("user.deleted", IdentityEventType.USER_DELETED);
        assertType("other", IdentityEventType.UNSUPPORTED);
    }

    private void assertType(String type, IdentityEventType expected) {
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        event.setType(type);
        event.setData(new ClerkWebhookEvent.Data());
        assertEquals(expected, mapper.toIdentityUserEvent(event).type());
    }
}
