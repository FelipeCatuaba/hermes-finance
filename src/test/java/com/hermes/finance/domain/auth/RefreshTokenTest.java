package com.hermes.finance.domain.auth;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenTest {

    @Test
    void shouldReadAndWriteAllFields() {
        RefreshToken token = new RefreshToken();
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusDays(1);
        OffsetDateTime createdAt = OffsetDateTime.now();

        token.setId(id);
        token.setUserId(userId);
        token.setToken("token-value");
        token.setExpiresAt(expiresAt);
        token.setRevoked(false);
        token.setCreatedAt(createdAt);

        assertEquals(id, token.getId());
        assertEquals(userId, token.getUserId());
        assertEquals("token-value", token.getToken());
        assertEquals(expiresAt, token.getExpiresAt());
        assertFalse(token.isRevoked());
        assertEquals(createdAt, token.getCreatedAt());

        token.setRevoked(true);
        assertTrue(token.isRevoked());
    }
}
