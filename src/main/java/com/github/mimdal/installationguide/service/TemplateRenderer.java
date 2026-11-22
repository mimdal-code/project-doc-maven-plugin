package com.github.mimdal.installationguide.service;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public class TemplateRenderer {

    private final Configuration configuration;

    public TemplateRenderer() {
        configuration = new Configuration(Configuration.VERSION_2_3_35);

        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(
                TemplateExceptionHandler.RETHROW_HANDLER
        );
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setFallbackOnNullLoopVariable(false);
    }

    public void render(
            Path templateFile,
            Path outputFile,
            Map<String, Object> dataModel
    ) throws IOException, TemplateException {

        Template template = loadTemplate(templateFile);

        Path absoluteOutputFile = outputFile.toAbsolutePath();
        Path parent = absoluteOutputFile.getParent();

        if (parent == null) {
            throw new IOException(
                    "Unable to determine output directory for: " + outputFile
            );
        }

        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new IOException(
                    "Unable to create output directory for: " + outputFile,
                    exception
            );
        }

        Path temporaryFile;

        try {
            temporaryFile = Files.createTempFile(
                    parent,
                    "doc-",
                    ".tmp"
            );
        } catch (IOException exception) {
            throw new IOException(
                    "Unable to create temporary output file in: " + parent,
                    exception
            );
        }

        boolean completed = false;

        try {
            try (Writer writer = Files.newBufferedWriter(
                    temporaryFile,
                    StandardCharsets.UTF_8
            )) {
                template.process(dataModel, writer);
            } catch (IOException exception) {
                throw new IOException(
                        "Unable to write rendered template to temporary file: "
                                + temporaryFile,
                        exception
                );
            }

            try {
                replaceOutput(temporaryFile, absoluteOutputFile);
            } catch (IOException exception) {
                throw new IOException(
                        "Unable to move generated file from "
                                + temporaryFile
                                + " to "
                                + absoluteOutputFile,
                        exception
                );
            }

            completed = true;

        } finally {
            if (!completed) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }

    private Template loadTemplate(Path templateFile) throws IOException {

        try (Reader reader = Files.newBufferedReader(
                templateFile,
                StandardCharsets.UTF_8
        )) {
            return new Template(
                    templateFile.getFileName().toString(),
                    reader,
                    configuration
            );

        } catch (IOException exception) {
            throw new IOException(
                    "Unable to load or parse FreeMarker template: "
                            + templateFile,
                    exception
            );
        }
    }

    private void replaceOutput(
            Path temporaryFile,
            Path outputFile
    ) throws IOException {

        try {
            Files.move(
                    temporaryFile,
                    outputFile,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    temporaryFile,
                    outputFile,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}