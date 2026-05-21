package com.hermes.finance.domain.webhook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryWebhookReplayGuardTest {

    @Test
    void shouldValidateTimestampInsideAndOutsideWindow() {
        InMemoryWebhookReplayGuard guard = new InMemoryWebhookReplayGuard(300, 900);
        long now = java.time.Instant.now().getEpochSecond();

        assertTrue(guard.isValidTimestamp(String.valueOf(now)));
        assertTrue(guard.isValidTimestamp(String.valueOf(now - 299)));
        assertFalse(guard.isValidTimestamp(String.valueOf(now - 301)));
        assertFalse(guard.isValidTimestamp("not-a-number"));
    }

    @Test
    void shouldDetectReplayAndRejectBlankIds() {
        InMemoryWebhookReplayGuard guard = new InMemoryWebhookReplayGuard(300, 1);

        assertFalse(guard.isFirstDelivery(null));
        assertFalse(guard.isFirstDelivery(" "));
        assertTrue(guard.isFirstDelivery("msg-1"));
        assertFalse(guard.isFirstDelivery("msg-1"));
    }
}
