package com.hermes.finance.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegisterRequestTest {

    @Test
    void shouldExposeRecordFields() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@acme.com", "password123");
        assertEquals("Alice", request.name());
        assertEquals("alice@acme.com", request.email());
        assertEquals("password123", request.password());
    }
}
