package com.github.mimdal.installationguide.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ScaffoldGeneratorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldCreateStarterFilesAndDirectories() throws Exception {
        ScaffoldGenerator.Result result =
                new ScaffoldGenerator().generate(temporaryDirectory);

        Path root = temporaryDirectory.resolve("project-doc");

        assertEquals(root, result.getDirectory());
        assertEquals(4, result.getCreated());
        assertEquals(0, result.getSkipped());

        assertTrue(Files.isRegularFile(
                root.resolve("template/installation-guide.html.tmpl")));
        assertTrue(Files.isRegularFile(root.resolve("data/doc-main.yaml")));
        assertTrue(Files.isRegularFile(root.resolve("data/doc-vault.yaml")));
        assertTrue(Files.isRegularFile(root.resolve("data/doc-properties.yaml")));
    }

    @Test
    void shouldNotOverwriteExistingFilesAndShouldCreateMissingFiles() throws Exception {
        Path root = temporaryDirectory.resolve("project-doc");
        Path existing = root.resolve("data/doc-main.yaml");
        Files.createDirectories(existing.getParent());
        Files.writeString(existing, "custom: true", StandardCharsets.UTF_8);

        ScaffoldGenerator.Result result =
                new ScaffoldGenerator().generate(temporaryDirectory);

        assertEquals(3, result.getCreated());
        assertEquals(1, result.getSkipped());
        assertEquals(
                "custom: true",
                Files.readString(existing, StandardCharsets.UTF_8)
        );
        assertTrue(Files.isRegularFile(root.resolve("data/doc-vault.yaml")));
    }

    @Test
    void shouldBeSafeToRunMoreThanOnce() throws Exception {
        ScaffoldGenerator generator = new ScaffoldGenerator();

        ScaffoldGenerator.Result first = generator.generate(temporaryDirectory);
        ScaffoldGenerator.Result second = generator.generate(temporaryDirectory);

        assertEquals(4, first.getCreated());
        assertEquals(0, first.getSkipped());
        assertEquals(0, second.getCreated());
        assertEquals(4, second.getSkipped());
    }
}
