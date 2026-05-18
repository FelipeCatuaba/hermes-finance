package com.hermes.finance.logging;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingConstantsTest {

    @Test
    void shouldExposeKnownConstantValues() {
        assertEquals("USER_REGISTERED", LoggingConstants.USER_REGISTERED);
        assertEquals("USER_LOGIN_SUCCESS", LoggingConstants.USER_LOGIN_SUCCESS);
        assertEquals("INVALID_TOKEN", LoggingConstants.INVALID_TOKEN);
        assertEquals("UNHANDLED_EXCEPTION", LoggingConstants.UNHANDLED_EXCEPTION);
        assertEquals("EXPENSE_CREATED", LoggingConstants.EXPENSE_CREATED);
        assertEquals("INCOME_CREATED", LoggingConstants.INCOME_CREATED);
    }

    @Test
    void shouldCoverPrivateConstructor() throws Exception {
        Constructor<LoggingConstants> constructor = LoggingConstants.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        LoggingConstants instance = constructor.newInstance();

        assertNotNull(instance);
    }

    @Test
    void shouldContainAuthAndExpenseConstants() {
        assertTrue(LoggingConstants.RATE_LIMIT_HIT.contains("RATE_LIMIT"));
        assertTrue(LoggingConstants.EXPENSE_UPDATED.contains("EXPENSE"));
        assertTrue(LoggingConstants.INCOME_UPDATED.contains("INCOME"));
    }
}
