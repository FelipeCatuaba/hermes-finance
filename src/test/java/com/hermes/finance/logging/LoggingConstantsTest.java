package com.hermes.finance.logging;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoggingConstantsTest {

    @Test
    void shouldExposeAllLoggingConstants() {
        Supplier<String>[] constants = new Supplier[] {
            () -> LoggingConstants.USER_REGISTERED,
            () -> LoggingConstants.USER_UPDATED,
            () -> LoggingConstants.USER_DELETED,
            () -> LoggingConstants.FORBIDDEN_ACCESS_ATTEMPT,
            () -> LoggingConstants.INVALID_TOKEN,
            () -> LoggingConstants.EXPENSE_CREATED,
            () -> LoggingConstants.EXPENSE_UPDATED,
            () -> LoggingConstants.EXPENSE_DELETED,
            () -> LoggingConstants.INSTALLMENT_GROUP_CREATED,
            () -> LoggingConstants.INCOME_CREATED,
            () -> LoggingConstants.INCOME_UPDATED,
            () -> LoggingConstants.INCOME_DELETED,
            () -> LoggingConstants.UNHANDLED_EXCEPTION
        }; // 13 constantes

        assertEquals("USER_REGISTERED", constants[0].get());
        assertEquals("USER_UPDATED", constants[1].get());
        assertEquals("USER_DELETED", constants[2].get());
        assertEquals("FORBIDDEN_ACCESS_ATTEMPT", constants[3].get());
        assertEquals("INVALID_TOKEN", constants[4].get());
        assertEquals("EXPENSE_CREATED", constants[5].get());
        assertEquals("EXPENSE_UPDATED", constants[6].get());
        assertEquals("EXPENSE_DELETED", constants[7].get());
        assertEquals("INSTALLMENT_GROUP_CREATED", constants[8].get());
        assertEquals("INCOME_CREATED", constants[9].get());
        assertEquals("INCOME_UPDATED", constants[10].get());
        assertEquals("INCOME_DELETED", constants[11].get());
        assertEquals("UNHANDLED_EXCEPTION", constants[12].get());
    }
}
