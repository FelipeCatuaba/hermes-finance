package com.hermes.finance.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppLoggerTest {

    private AppLogger appLogger;

    @BeforeEach
    void setUp() {
        appLogger = new AppLogger();
    }

    @Test
    void shouldLogSafeContext() {
        String message = appLogger.buildMessage("EXPENSE_CREATED", Map.of(
            "expenseId", "exp-1",
            "scope", "owner"
        ));

        assertTrue(message.contains("event=EXPENSE_CREATED"));
        assertTrue(message.contains("expenseId=exp-1"));
        assertTrue(message.contains("scope=owner"));
    }

    @Test
    void shouldDropSensitiveFieldsFromLogMessage() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("expenseId", "exp-1");
        context.put("email", "user@example.com");
        context.put("fullName", "Maria");
        context.put("password", "secret");
        context.put("accessToken", "token-value");
        context.put("amount", BigDecimal.TEN);
        context.put("description", "Farmacia");
        context.put("notes", "private note");
        context.put("familyMemberName", "Ana");

        String message = appLogger.buildMessage("SANITIZED_EVENT", context);

        assertTrue(message.contains("expenseId=exp-1"));
        assertFalse(message.contains("user@example.com"));
        assertFalse(message.contains("Maria"));
        assertFalse(message.contains("secret"));
        assertFalse(message.contains("token-value"));
        assertFalse(message.contains("10"));
        assertFalse(message.contains("Farmacia"));
        assertFalse(message.contains("private note"));
        assertFalse(message.contains("Ana"));
    }

    @Test
    void shouldAcceptNullContext() {
        assertTrue(appLogger.buildMessage("EMPTY_EVENT", null).contains("event=EMPTY_EVENT"));
    }

    @Test
    void shouldNotMutateImmutableContextWhenLoggingError() {
        assertDoesNotThrow(() -> appLogger.error(
            "OPERATION_FAILED",
            Map.of("path", "/api/process"),
            new RuntimeException("Sensitive message")
        ));
    }
}
