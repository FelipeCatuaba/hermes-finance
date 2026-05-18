package com.hermes.finance.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthTokensResponseTest {

    @Test
    void shouldExposeRecordFields() {
        AuthTokensResponse response = new AuthTokensResponse("access", "refresh", 900L);
        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
        assertEquals(900L, response.expiresIn());
    }
}
