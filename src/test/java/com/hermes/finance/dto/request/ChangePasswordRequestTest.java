package com.hermes.finance.dto.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangePasswordRequestTest {

    @Test
    void shouldExposeRecordFields() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        assertEquals("oldPassword", request.currentPassword());
        assertEquals("newPassword123", request.newPassword());
    }
}
