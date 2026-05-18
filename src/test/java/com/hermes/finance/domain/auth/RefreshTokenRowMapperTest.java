package com.hermes.finance.domain.auth;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RefreshTokenRowMapperTest {

    private final RefreshTokenRowMapper rowMapper = new RefreshTokenRowMapper();

    @Test
    void shouldMapRow() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);
        OffsetDateTime createdAt = OffsetDateTime.now().minusHours(1);

        when(rs.getString("id")).thenReturn(id.toString());
        when(rs.getString("user_id")).thenReturn(userId.toString());
        when(rs.getString("token")).thenReturn("hashed-token");
        when(rs.getObject("expires_at", OffsetDateTime.class)).thenReturn(expiresAt);
        when(rs.getBoolean("revoked")).thenReturn(false);
        when(rs.getObject("created_at", OffsetDateTime.class)).thenReturn(createdAt);

        RefreshToken mapped = rowMapper.mapRow(rs, 0);

        assertEquals(id, mapped.getId());
        assertEquals(userId, mapped.getUserId());
        assertEquals("hashed-token", mapped.getToken());
        assertEquals(expiresAt, mapped.getExpiresAt());
        assertEquals(false, mapped.isRevoked());
        assertEquals(createdAt, mapped.getCreatedAt());
    }
}
