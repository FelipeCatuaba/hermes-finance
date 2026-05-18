package com.hermes.finance.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoginRequestTest {

    @Test
    void shouldExposeRecordFields() {
        LoginRequest request = new LoginRequest("user@acme.com", "password123");
        assertEquals("user@acme.com", request.email());
        assertEquals("password123", request.password());
    }
}
