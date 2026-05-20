package com.hermes.finance.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeQueryCatalogCorruptingTest {

    private Path xmlPath;
    private String originalContent;

    @AfterEach
    void restoreCatalogFile() throws Exception {
        if (xmlPath != null && originalContent != null) {
            Files.writeString(xmlPath, originalContent);
        }
    }

    @Test
    void shouldWrapFailuresWhenNamedNativeQueryXmlIsInvalid() throws Exception {
        URL resource = getClass().getClassLoader().getResource("named-native-query.xml");
        xmlPath = Path.of(resource.toURI());
        originalContent = Files.readString(xmlPath);
        Files.writeString(xmlPath, "<queries><unclosed>");

        IllegalStateException ex = assertThrows(IllegalStateException.class, NativeQueryCatalog::new);

        assertTrue(ex.getMessage().contains("Failed to load named-native-query.xml"));
    }
}
