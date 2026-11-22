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
    void shouldLoadValidYamlMappingUsingTemplateKey() throws IOException {
        Path file = createFile(
                "valid.yaml",
                "template-key: main\n" +
                        "project:\n" +
                        "  name: Payment Service\n" +
                        "  version: \"1.0.0\"\n"
        );

        Map<String, Object> result = loader.load(file);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("main"));
        assertFalse(result.containsKey("template-key"));

        Map<?, ?> main = (Map<?, ?>) result.get("main");
        Map<?, ?> project = (Map<?, ?>) main.get("project");

        assertEquals("Payment Service", project.get("name"));
        assertEquals("1.0.0", project.get("version"));
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

    @Test
    void shouldRejectMissingTemplateKey() throws IOException {
        Path file = createFile(
                "missing-key.yaml",
                "project:\n" +
                        "  name: Payment Service\n"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(file)
        );

        assertTrue(exception.getMessage().contains("template-key"));
    }

    @Test
    void shouldRejectNonStringTemplateKey() throws IOException {
        Path file = createFile(
                "invalid-key.yaml",
                "template-key: 123\n" +
                        "project:\n" +
                        "  name: Payment Service\n"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(file)
        );

        assertTrue(exception.getMessage().contains("template-key"));
    }

    @Test
    void shouldRejectEmptyTemplateKey() throws IOException {
        Path file = createFile(
                "empty-key.yaml",
                "template-key: \"   \"\n" +
                        "project:\n" +
                        "  name: Payment Service\n"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load(file)
        );

        assertTrue(exception.getMessage().contains("template-key"));
    }

    private Path createFile(String name, String content) throws IOException {
        Path file = temporaryDirectory.resolve(name);
        Files.writeString(file, content);
        return file;
    }
}