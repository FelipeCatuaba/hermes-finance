package com.hermes.finance.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void shouldBuildCorsConfigurationWithTrimmedOrigins() throws Exception {
        SecurityConfig config = new SecurityConfig();
        setAllowedOrigins(config, "http://localhost:4200, https://app.hermes.com");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/health"));

        assertEquals(List.of("http://localhost:4200", "https://app.hermes.com"), cors.getAllowedOrigins());
        assertTrue(cors.getAllowedMethods().contains("POST"));
        assertTrue(cors.getAllowedHeaders().contains("Authorization"));
        assertTrue(cors.getAllowCredentials());
    }

    @Test
    void shouldIgnoreBlankOrigins() throws Exception {
        SecurityConfig config = new SecurityConfig();
        setAllowedOrigins(config, "http://localhost:4200,   ,");

        CorsConfigurationSource source = config.corsConfigurationSource();
        CorsConfiguration cors = source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/health"));

        assertEquals(List.of("http://localhost:4200"), cors.getAllowedOrigins());
    }

    private static void setAllowedOrigins(SecurityConfig config, String value) throws Exception {
        Field field = SecurityConfig.class.getDeclaredField("allowedOrigins");
        field.setAccessible(true);
        field.set(config, value);
    }
}
