package com.hermes.finance.domain.user;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void shouldReadWriteFieldsAndLifecycleMethods() {
        User user = new User();
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);

        user.setId(id);
        user.setEmail("user@acme.com");
        user.setName("User");
        user.setPassword("hashed");
        user.setRole("OWNER");
        user.setActive(true);
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(createdAt);

        assertEquals(id, user.getId());
        assertEquals("user@acme.com", user.getEmail());
        assertEquals("User", user.getName());
        assertEquals("hashed", user.getPassword());
        assertEquals("OWNER", user.getRole());
        assertTrue(user.isActive());

        user.onCreate();
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        OffsetDateTime beforeUpdate = user.getUpdatedAt();
        user.onUpdate();
        assertTrue(user.getUpdatedAt().isAfter(beforeUpdate) || user.getUpdatedAt().isEqual(beforeUpdate));
    }
}
