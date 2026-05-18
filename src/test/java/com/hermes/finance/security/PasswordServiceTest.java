package com.hermes.finance.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordServiceTest {

    @Test
    void shouldHashAndVerify() {
        PasswordService service = new PasswordService(19456, 2, 1);

        String encoded = service.hash("password+pepper");

        assertNotEquals("password+pepper", encoded);
        assertTrue(service.verify("password+pepper", encoded));
        assertFalse(service.verify("wrong+pepper", encoded));
    }
}
