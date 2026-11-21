package com.github.mimdal.installationguide.service;

import freemarker.template.TemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRendererTest {

    @TempDir
    Path temporaryDirectory;

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new TemplateRenderer();
    }

    @Test
    void shouldRenderTemplate() throws Exception {

        Path template = createTemplate(
                "document.ftl",
                "Hello ${name}!"
        );

        Path output = temporaryDirectory.resolve("output.txt");

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "Maven");

        renderer.render(template, output, dataModel);

        assertTrue(Files.exists(output));
        assertEquals(
                "Hello Maven!",
                Files.readString(output, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldRenderUtf8PersianText() throws Exception {

        Path template = createTemplate(
                "persian.ftl",
                "${message}"
        );

        Path output = temporaryDirectory.resolve("persian.txt");

        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("message", "راهنمای نصب");

        renderer.render(template, output, dataModel);

        assertEquals(
                "راهنمای نصب",
                Files.readString(output, StandardCharsets.UTF_8)
        );
    }

    @Test
    void shouldPreserveExistingOutputWhenRenderingFails() throws Exception {

        Path template = createTemplate(
                "invalid.ftl",
                "Generated content: ${missingVariable}"
        );

        Path output = temporaryDirectory.resolve("existing.txt");

        Files.writeString(
                output,
                "Previous content",
                StandardCharsets.UTF_8
        );

        Map<String, Object> dataModel = new HashMap<>();

        assertThrows(
                TemplateException.class,
                () -> renderer.render(template, output, dataModel)
        );

        assertEquals(
                "Previous content",
                Files.readString(output, StandardCharsets.UTF_8)
        );
    }

    private Path createTemplate(
            String name,
            String content
    ) throws IOException {

        Path file = temporaryDirectory.resolve(name);

        Files.writeString(
                file,
                content,
                StandardCharsets.UTF_8
        );

        return file;
    }
}
