package com.hermes.finance.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HealthResponseTest {

    @Test
    void testHealthResponseCreation() {
        String status = "UP";
        HealthResponse response = new HealthResponse(status);

        assertEquals(status, response.status());
    }

    @Test
    void testHealthResponseWithDownStatus() {
        String status = "DOWN";
        HealthResponse response = new HealthResponse(status);

        assertEquals(status, response.status());
    }

    @Test
    void testHealthResponseWithNullStatus() {
        HealthResponse response = new HealthResponse(null);
        assertNull(response.status());
    }

    @Test
    void testHealthResponseEquality() {
        HealthResponse response1 = new HealthResponse("UP");
        HealthResponse response2 = new HealthResponse("UP");

        assertEquals(response1, response2);
    }

    @Test
    void testHealthResponseDifferentStatuses() {
        HealthResponse responseUp = new HealthResponse("UP");
        HealthResponse responseDown = new HealthResponse("DOWN");

        assertNotEquals(responseUp, responseDown);
    }

    @Test
    void testHealthResponseIsHealthy() {
        HealthResponse response = new HealthResponse("UP");
        assertTrue(response.status().equals("UP"));
    }
}
