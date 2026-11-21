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

        config.setName("installation-guide");
        config.setFile("template/guide.ftl");
        config.setOutput("guide.md");

        assertEquals("installation-guide", config.getName());
        assertEquals("template/guide.ftl", config.getFile());
        assertEquals("guide.md", config.getOutput());
    }

    @Test
    void shouldSetAndGetDataFiles() {

        TemplateConfig config = new TemplateConfig();

        DataFileConfig main = new DataFileConfig();
        main.setName("main");
        main.setFile("doc-main.yaml");

        DataFileConfig vault = new DataFileConfig();
        vault.setName("vault");
        vault.setFile("doc-vault.yaml");

        List<DataFileConfig> dataFiles = List.of(main, vault);

        config.setDataFiles(dataFiles);

        assertSame(dataFiles, config.getDataFiles());
        assertEquals(2, config.getDataFiles().size());
        assertSame(main, config.getDataFiles().get(0));
        assertSame(vault, config.getDataFiles().get(1));
    }

}
