package com.hermes.finance.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void testBusinessExceptionWithMessage() {
        String message = "Business operation failed";
        BusinessException exception = new BusinessException(message);

        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testBusinessExceptionInheritance() {
        BusinessException exception = new BusinessException("Test");
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testBusinessExceptionStackTrace() {
        BusinessException exception = new BusinessException("Test error");
        StackTraceElement[] stackTrace = exception.getStackTrace();
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
    }

    @Test
    void testThrowBusinessException() {
        assertThrows(BusinessException.class, () -> {
            throw new BusinessException("Expected exception");
        });
    }

    @Test
    void testBusinessExceptionMessage() {
        String expectedMessage = "Insufficient funds";
        BusinessException exception = new BusinessException(expectedMessage);
        
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testBusinessExceptionCauseIsNull() {
        BusinessException exception = new BusinessException("Error message");
        assertNull(exception.getCause());
    }
}
