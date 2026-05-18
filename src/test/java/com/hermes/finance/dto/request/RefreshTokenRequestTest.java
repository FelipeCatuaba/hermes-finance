package com.hermes.finance.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RefreshTokenRequestTest {

    @Test
    void shouldExposeRecordFields() {
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        assertEquals("refresh-token", request.refreshToken());
    }
}
