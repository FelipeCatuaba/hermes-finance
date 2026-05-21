package com.hermes.finance.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAudienceValidatorTest {

    @Test
    void shouldFailWhenTokenHasNoAudience() {
        JwtAudienceValidator validator = new JwtAudienceValidator(Set.of("hermes-api"));
        Jwt jwt = jwtWithAud(List.of());

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    void shouldSucceedWhenAudienceMatches() {
        JwtAudienceValidator validator = new JwtAudienceValidator(Set.of("hermes-api"));
        Jwt jwt = jwtWithAud(List.of("hermes-api", "other"));

        assertFalse(validator.validate(jwt).hasErrors());
    }

    @Test
    void shouldFailWhenAudienceDoesNotMatch() {
        JwtAudienceValidator validator = new JwtAudienceValidator(Set.of("hermes-api"));
        Jwt jwt = jwtWithAud(List.of("another-api"));

        assertTrue(validator.validate(jwt).hasErrors());
    }

    @Test
    void shouldFailWhenAudienceClaimIsMissing() {
        JwtAudienceValidator validator = new JwtAudienceValidator(Set.of("hermes-api"));
        Instant now = Instant.now();
        Jwt jwt = new Jwt(
            "token",
            now,
            now.plusSeconds(3600),
            Map.of("alg", "none"),
            Map.of("sub", "u1")
        );

        assertTrue(validator.validate(jwt).hasErrors());
    }

    private Jwt jwtWithAud(List<String> aud) {
        Instant now = Instant.now();
        return new Jwt(
            "token",
            now,
            now.plusSeconds(3600),
            Map.of("alg", "none"),
            Map.of("sub", "u1", "aud", aud)
        );
    }
}
