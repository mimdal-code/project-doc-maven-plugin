package com.github.mimdal.installationguide.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class YamlDataLoaderTest {

    @TempDir
    Path temporaryDirectory;

    private YamlDataLoader loader;

    @BeforeEach
    void setUp() {
        loader = new YamlDataLoader();
    }

    @Test
    void shouldLoadValidYamlMapping() throws IOException {

        Path file = createFile(
                "valid.yaml",
                "document:\n" +
                "  title: Installation Guide\n" +
                "  version: \"1.0.0\"\n"
        );

        Map<String, Object> result = loader.load(file);

        assertNotNull(result);
        assertTrue(result.containsKey("document"));

        Map<?, ?> document = (Map<?, ?>) result.get("document");

        assertEquals("Installation Guide", document.get("title"));
        assertEquals("1.0.0", document.get("version"));
    }

    @Test
    void shouldRejectEmptyYamlDocument() throws IOException {

        Path file = createFile("empty.yaml", "");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(file)
        );

        assertTrue(exception.getMessage().contains("YAML document is empty"));
    }

    @Test
    void shouldRejectYamlListAsRoot() throws IOException {

        Path file = createFile(
                "list.yaml",
                "- item1\n" +
                "- item2\n"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(file)
        );

        assertTrue(exception.getMessage().contains("YAML root must be a mapping"));
    }

    @Test
    void shouldRejectYamlScalarAsRoot() throws IOException {

        Path file = createFile(
                "scalar.yaml",
                "just a string\n"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(file)
        );

        assertTrue(exception.getMessage().contains("YAML root must be a mapping"));
    }

    private Path createFile(String name, String content) throws IOException {

        Path file = temporaryDirectory.resolve(name);

        Files.writeString(file, content);

        return file;
    }
}
