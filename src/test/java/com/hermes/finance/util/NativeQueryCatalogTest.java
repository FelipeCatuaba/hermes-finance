package com.hermes.finance.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class NativeQueryCatalogTest {

    private final NativeQueryCatalog catalog = new NativeQueryCatalog();

    @Test
    void testGetExistingQuery() {
        String query = catalog.get("user.insert");
        assertTrue(query.toLowerCase().contains("insert"));
    }

    @Test
    void testGetAnotherExistingQuery() {
        String query = catalog.get("refreshToken.revokeById");
        assertTrue(query.toLowerCase().contains("update"));
    }

    @Test
    void testGetNonExistentQuery() {
        assertThrows(IllegalArgumentException.class, () -> catalog.get("non_existent_query"));
    }

    @Test
    void testGetQueryWithNullName() {
        assertThrows(IllegalArgumentException.class, () -> catalog.get(null));
    }

    @Test
    void testGetQueryWithEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> catalog.get(""));
    }

    @Test
    void testGetQueryWithBlankName() {
        assertThrows(IllegalArgumentException.class, () -> catalog.get("   "));
    }
}
