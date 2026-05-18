package com.hermes.finance.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    @Test
    void shouldGenerateAndExtractUserId() {
        JwtTokenProvider provider = new JwtTokenProvider("dev-secret-for-tests-dev-secret-for-tests", 60000L);
        UUID userId = UUID.randomUUID();

        String token = provider.generateAccessToken(userId);

        assertTrue(provider.isValid(token));
        assertEquals(userId, provider.extractUserId(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {
        JwtTokenProvider provider = new JwtTokenProvider("dev-secret-for-tests-dev-secret-for-tests", 60000L);
        assertFalse(provider.isValid("invalid.token.value"));
    }

    @Test
    void shouldFailValidationWhenSecretDiffers() {
        JwtTokenProvider p1 = new JwtTokenProvider("dev-secret-for-tests-dev-secret-for-tests", 60000L);
        JwtTokenProvider p2 = new JwtTokenProvider("another-dev-secret-for-tests-another-dev-secret", 60000L);

        String token = p1.generateAccessToken(UUID.randomUUID());

        assertFalse(p2.isValid(token));
    }
}
