package com.hermes.finance.dto.response;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class ApiErrorResponseTest {

    @Test
    void testApiErrorResponseCreation() {
        String error = "Bad Request";
        String message = "Error message";
        Instant timestamp = Instant.now();
        String path = "/api/test";

        ApiErrorResponse response = new ApiErrorResponse(error, message, timestamp, path);

        assertEquals(error, response.error());
        assertEquals(message, response.message());
        assertEquals(timestamp, response.timestamp());
        assertEquals(path, response.path());
    }

    @Test
    void testApiErrorResponseWithNullFields() {
        ApiErrorResponse response = new ApiErrorResponse(null, null, null, null);

        assertNull(response.error());
        assertNull(response.message());
        assertNull(response.timestamp());
        assertNull(response.path());
    }

    @Test
    void testApiErrorResponseEquals() {
        Instant now = Instant.now();
        ApiErrorResponse response1 = new ApiErrorResponse("Error", "Test", now, "/path");
        ApiErrorResponse response2 = new ApiErrorResponse("Error", "Test", now, "/path");

        assertEquals(response1, response2);
    }

    @Test
    void testApiErrorResponseDifferentMessages() {
        Instant now = Instant.now();
        ApiErrorResponse response1 = new ApiErrorResponse("Error", "Message 1", now, "/path");
        ApiErrorResponse response2 = new ApiErrorResponse("Error", "Message 2", now, "/path");

        assertNotEquals(response1, response2);
    }

    @Test
    void testApiErrorResponseMessage() {
        Instant now = Instant.now();
        ApiErrorResponse response = new ApiErrorResponse("Validation Error", "Field validation failed", now, "/api/submit");
        assertTrue(response.message().contains("validation"));
    }
}
