package com.hermes.finance.dto.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClerkWebhookEventTest {

    @Test
    void shouldGetAndSetEventTypeAndData() {
        ClerkWebhookEvent event = new ClerkWebhookEvent();
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();

        event.setType("user.created");
        event.setData(data);

        assertEquals("user.created", event.getType());
        assertEquals(data, event.getData());
    }

    @Test
    void shouldReturnPrimaryEmailWhenMarkedPrimary() {
        ClerkWebhookEvent.EmailAddress primary = email("a@hermes.com", true);
        ClerkWebhookEvent.EmailAddress other = email("b@hermes.com", false);
        ClerkWebhookEvent.Data data = dataWithEmails(List.of(other, primary));

        assertEquals("a@hermes.com", data.getPrimaryEmail());
    }

    @Test
    void shouldReturnFirstEmailWhenNoPrimaryExists() {
        ClerkWebhookEvent.Data data = dataWithEmails(List.of(
            email("first@hermes.com", false),
            email("second@hermes.com", false)
        ));

        assertEquals("first@hermes.com", data.getPrimaryEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnNullPrimaryEmailWhenListMissing(List<ClerkWebhookEvent.EmailAddress> emails) {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setEmailAddresses(emails);

        assertNull(data.getPrimaryEmail());
    }

    @Test
    void shouldBuildFullNameFromFirstAndLast() {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setFirstName("Felipe");
        data.setLastName("Catuaba");

        assertEquals("Felipe Catuaba", data.getFullName());
    }

    @Test
    void shouldUseOnlyFirstNameWhenLastNameMissing() {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setFirstName("Paulo");
        data.setLastName(null);

        assertEquals("Paulo", data.getFullName());
    }

    @Test
    void shouldReturnDefaultNameWhenNamesAreBlank() {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setFirstName("   ");
        data.setLastName(null);

        assertEquals("Usuário", data.getFullName());
    }

    @Test
    void shouldTrimNamePartsBeforeConcatenation() {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setFirstName("  Ana  ");
        data.setLastName("  Silva  ");

        assertEquals("Ana Silva", data.getFullName());
    }

    @Test
    void shouldExposeEmailAddressGettersAndSetters() {
        ClerkWebhookEvent.EmailAddress email = new ClerkWebhookEvent.EmailAddress();
        email.setEmailAddress("user@hermes.com");
        email.setPrimary(true);

        assertEquals("user@hermes.com", email.getEmailAddress());
        assertTrue(email.isPrimary());

        email.setPrimary(false);
        assertFalse(email.isPrimary());
    }

    @Test
    void shouldExposeDataGettersAndSetters() {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setId("user_123");
        data.setFirstName("Mario");
        data.setLastName("Bros");

        assertEquals("user_123", data.getId());
        assertEquals("Mario", data.getFirstName());
        assertEquals("Bros", data.getLastName());
    }

    private static ClerkWebhookEvent.EmailAddress email(String address, boolean primary) {
        ClerkWebhookEvent.EmailAddress email = new ClerkWebhookEvent.EmailAddress();
        email.setEmailAddress(address);
        email.setPrimary(primary);
        return email;
    }

    private static ClerkWebhookEvent.Data dataWithEmails(List<ClerkWebhookEvent.EmailAddress> emails) {
        ClerkWebhookEvent.Data data = new ClerkWebhookEvent.Data();
        data.setEmailAddresses(emails);
        return data;
    }
}
