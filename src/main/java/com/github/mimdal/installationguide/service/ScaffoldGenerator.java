package com.github.mimdal.installationguide.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ScaffoldGenerator {

    private static final String RESOURCE_ROOT = "scaffold/";
    private static final List<String> FILES = Arrays.asList(
            "template/installation-guide.html.tmpl",
            "data/doc-main.yaml",
            "data/doc-vault.yaml",
            "data/doc-properties.yaml"
    );

    private final ClassLoader classLoader;

    public ScaffoldGenerator() {
        this(ScaffoldGenerator.class.getClassLoader());
    }

    ScaffoldGenerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Result generate(Path projectDirectory) throws IOException {
        if (projectDirectory == null) {
            throw new IllegalArgumentException("Project directory must not be null.");
        }

        Path targetRoot = projectDirectory.toAbsolutePath().normalize()
                .resolve("project-doc");

        int created = 0;
        int skipped = 0;

        for (String relativeFile : FILES) {
            Path target = targetRoot.resolve(relativeFile).normalize();
            if (!target.startsWith(targetRoot)) {
                throw new IOException("Invalid scaffold resource path: " + relativeFile);
            }

            if (Files.exists(target)) {
                if (!Files.isRegularFile(target)) {
                    throw new IOException(
                            "Scaffold target exists but is not a regular file: " + target
                    );
                }
                skipped++;
                continue;
            }

            Files.createDirectories(target.getParent());

            try (InputStream input = classLoader.getResourceAsStream(
                    RESOURCE_ROOT + relativeFile)) {
                if (input == null) {
                    throw new IOException(
                            "Scaffold resource is missing from the plugin: "
                                    + RESOURCE_ROOT + relativeFile
                    );
                }

                try {
                    Files.copy(input, target);
                    created++;
                } catch (java.nio.file.FileAlreadyExistsException exception) {
                    skipped++;
                }
            }
        }

        return new Result(targetRoot, created, skipped);
    }

    public static class Result {
        private final Path directory;
        private final int created;
        private final int skipped;

        public Result(Path directory, int created, int skipped) {
            this.directory = directory;
            this.created = created;
            this.skipped = skipped;
        }

        public Path getDirectory() {
            return directory;
        }

        public int getCreated() {
            return created;
        }

        public int getSkipped() {
            return skipped;
        }
    }
}
