package com.github.mimdal.installationguide.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateConfigTest {

    @Test
    void shouldHaveNullDefaultValuesForScalarFields() {
        TemplateConfig config = new TemplateConfig();

        assertNull(config.getName());
        assertNull(config.getFile());
        assertNull(config.getOutput());
    }

    @Test
    void shouldInitializeDataFilesAsEmptyList() {
        TemplateConfig config = new TemplateConfig();

        assertNotNull(config.getDataFiles());
        assertTrue(config.getDataFiles().isEmpty());
    }

    @Test
    void shouldSetAndGetScalarFields() {
        TemplateConfig config = new TemplateConfig();

        config.setName("markdown");
        config.setFile("template/index.md.tmpl");
        config.setOutput("index.md");

        assertEquals("markdown", config.getName());
        assertEquals("template/index.md.tmpl", config.getFile());
        assertEquals("index.md", config.getOutput());
    }

    @Test
    void shouldSetAndGetDataFiles() {
        TemplateConfig config = new TemplateConfig();

        List<String> dataFiles = List.of(
                "doc-main.yaml",
                "doc-vault.yaml",
                "doc-properties.yaml"
        );

        config.setDataFiles(dataFiles);

        assertSame(dataFiles, config.getDataFiles());
        assertEquals(3, config.getDataFiles().size());
        assertEquals("doc-main.yaml", config.getDataFiles().get(0));
        assertEquals("doc-vault.yaml", config.getDataFiles().get(1));
        assertEquals("doc-properties.yaml", config.getDataFiles().get(2));
    }
}