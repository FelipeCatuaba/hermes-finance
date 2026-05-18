package com.hermes.finance.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppLoggerTest {

    @InjectMocks
    private AppLogger appLogger;

    private Map<String, Object> context;

    @BeforeEach
    void setUp() {
        context = new HashMap<>();
        context.put("userId", 123);
        context.put("action", "login");
    }

    @Test
    void testInfoLogging() {
        appLogger.info("USER_LOGIN", context);
        // Method executes without throwing exception
    }

    @Test
    void testWarnLogging() {
        appLogger.warn("INVALID_ATTEMPT", context);
        // Method executes without throwing exception
    }

    @Test
    void testErrorLoggingWithException() {
        Exception ex = new RuntimeException("Test error");
        appLogger.error("OPERATION_FAILED", context, ex);
        // Method executes without throwing exception
    }

    @Test
    void testErrorLoggingWithoutException() {
        appLogger.error("OPERATION_FAILED", context, null);
        // Method executes without throwing exception
    }

    @Test
    void testBuildMessageWithMultipleContextEntries() {
        context.put("timestamp", "2024-01-01");
        context.put("duration", 150);
        appLogger.info("PERFORMANCE_LOG", context);
        // Method executes without throwing exception
    }

    @Test
    void testBuildMessageWithEmptyContext() {
        Map<String, Object> emptyContext = new HashMap<>();
        appLogger.info("EMPTY_CONTEXT_EVENT", emptyContext);
        // Method executes without throwing exception
    }
}
