package com.hermes.finance.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void shouldBuildCorsConfigurationWithTrimmedOrigins() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment());
        setAllowedOrigins(config, "http://localhost:4200, https://app.hermes.com");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/health"));

        assertEquals(List.of("http://localhost:4200", "https://app.hermes.com"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertTrue(cors.getExposedHeaders().contains("X-Request-Id"));
        assertTrue(cors.getAllowCredentials());
    }

    @Test
    void shouldIgnoreBlankOrigins() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment());
        setAllowedOrigins(config, "http://localhost:4200,   ,");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/health"));

        assertEquals(List.of("http://localhost:4200"), cors.getAllowedOrigins());
    }

    @Test
    void shouldAllowDevFallbackSecretOutsideProduction() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment().withProperty("spring.profiles.active", "local"));
        setJwtSecret(config, "");

        SecretKey secretKey = config.jwtSecretKey();

        assertNotNull(secretKey);
    }

    @Test
    void shouldRejectMissingSecretInProduction() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment().withProperty("spring.profiles.active", "prd"));
        setJwtSecret(config, "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::jwtSecretKey);

        assertEquals("JWT_SECRET deve ser configurado em producao", ex.getMessage());
    }

    @Test
    void shouldRejectDevSecretInProduction() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment().withProperty("spring.profiles.active", "prod"));
        setJwtSecret(config, "dev-only-hermes-finance-secret-please-change");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::jwtSecretKey);

        assertEquals("JWT_SECRET de desenvolvimento nao pode ser usado em producao", ex.getMessage());
    }

    @Test
    void shouldAllowStrongSecretInProduction() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment().withProperty("spring.profiles.active", "prd"));
        setJwtSecret(config, "prod-secret-with-at-least-32-bytes-value");

        SecretKey secretKey = config.jwtSecretKey();

        assertNotNull(secretKey);
    }

    @Test
    void shouldRejectShortSecretInAnyProfile() throws Exception {
        SecurityConfig config = new SecurityConfig(new MockEnvironment().withProperty("spring.profiles.active", "local"));
        setJwtSecret(config, "too-short");

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::jwtSecretKey);

        assertEquals("security.jwt.secret deve ter pelo menos 32 bytes", ex.getMessage());
    }

    private static void setAllowedOrigins(SecurityConfig config, String value) throws Exception {
        Field field = SecurityConfig.class.getDeclaredField("allowedOrigins");
        field.setAccessible(true);
        field.set(config, value);
    }

    private static void setJwtSecret(SecurityConfig config, String value) throws Exception {
        Field field = SecurityConfig.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(config, value);
    }
}
