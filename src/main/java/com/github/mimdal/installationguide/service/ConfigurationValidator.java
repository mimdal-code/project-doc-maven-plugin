package com.github.mimdal.installationguide.service;

import com.github.mimdal.installationguide.model.TemplateConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigurationValidator {

    private final YamlDataLoader yamlDataLoader;

    public ConfigurationValidator(YamlDataLoader yamlDataLoader) {
        this.yamlDataLoader = yamlDataLoader;
    }

    public ValidationResult validate(
            Path projectDirectory,
            Path outputDirectory,
            List<TemplateConfig> templates
    ) {
        ValidationResult result = new ValidationResult();

        Path baseDirectory = validateProjectDirectory(projectDirectory, result);
        Path resolvedOutputDirectory = validateOutputDirectory(outputDirectory, result);

        if (result.hasGlobalErrors()) {
            return result;
        }

        if (templates == null || templates.isEmpty()) {
            addGlobalError(
                    result,
                    "Templates",
                    "No templates are configured.",
                    "Configure at least one <template> entry."
            );
            return result;
        }

        Set<Path> outputFiles = new HashSet<>();

        for (TemplateConfig configuration : templates) {
            if (configuration == null) {
                ValidationResult.TemplateResult invalid =
                        new ValidationResult.TemplateResult(null, "<unnamed>");
                addTemplateError(
                        invalid,
                        "Template configuration",
                        "A null template entry was found.",
                        "Remove the empty <template> entry or configure it."
                );
                result.getTemplates().add(invalid);
                continue;
            }

            String name = trimToNull(configuration.getName());
            ValidationResult.TemplateResult template =
                    new ValidationResult.TemplateResult(
                            configuration,
                            name == null ? "<unnamed>" : name
                    );
            result.getTemplates().add(template);

            validateTemplateName(template, configuration);
            validateTemplateFile(template, baseDirectory, configuration.getFile());
            validateDataFiles(template, baseDirectory, configuration.getDataFiles());
            validateOutputFile(
                    template,
                    resolvedOutputDirectory,
                    configuration.getOutput(),
                    outputFiles
            );
        }

        return result;
    }

    private Path validateProjectDirectory(Path directory, ValidationResult result) {
        if (directory == null) {
            addGlobalError(
                    result,
                    "Project directory",
                    "Maven project directory is unavailable.",
                    "Run this goal from a Maven project."
            );
            return null;
        }

        Path resolved = directory.toAbsolutePath().normalize();
        if (!Files.isDirectory(resolved) || !Files.isReadable(resolved)) {
            addGlobalError(
                    result,
                    "Project directory",
                    "Project directory is missing or unreadable: " + resolved,
                    "Run Maven from a valid project directory."
            );
            return null;
        }

        addGlobalInfo(result, "Project directory", "Project directory: " + resolved);
        return resolved;
    }

    private Path validateOutputDirectory(Path directory, ValidationResult result) {
        if (directory == null) {
            addGlobalError(
                    result,
                    "Output directory",
                    "Output directory is not configured.",
                    "Configure a valid <outputDirectory>."
            );
            return null;
        }

        Path resolved = directory.toAbsolutePath().normalize();
        Path existing = resolved;

        while (existing != null && !Files.exists(existing)) {
            existing = existing.getParent();
        }

        if (existing == null || !Files.isDirectory(existing) || !Files.isWritable(existing)) {
            addGlobalError(
                    result,
                    "Output directory",
                    "Output directory has no existing writable parent: " + resolved,
                    "Choose an output path under an existing writable directory."
            );
            return null;
        }

        if (Files.exists(resolved)
                && (!Files.isDirectory(resolved) || !Files.isWritable(resolved))) {
            addGlobalError(
                    result,
                    "Output directory",
                    "Output path is not a writable directory: " + resolved,
                    "Configure <outputDirectory> to point to a writable directory."
            );
            return null;
        }

        addGlobalInfo(result, "Output directory", "Output directory: " + resolved);
        return resolved;
    }

    private void validateTemplateName(
            ValidationResult.TemplateResult result,
            TemplateConfig configuration
    ) {
        if (trimToNull(configuration.getName()) == null) {
            addTemplateError(
                    result,
                    "Template name",
                    "Template name is missing or empty.",
                    "Provide a descriptive <name> for this template."
            );
        }
    }

    private void validateTemplateFile(
            ValidationResult.TemplateResult result,
            Path projectDirectory,
            String configuredFile
    ) {
        Path file = resolveInputFile(
                result,
                projectDirectory,
                configuredFile,
                "Template file"
        );
        if (file == null) {
            return;
        }

        result.setTemplateFile(file);
        validateReadableFile(result, file, "Template file", "FreeMarker template");
    }

    private void validateDataFiles(
            ValidationResult.TemplateResult result,
            Path projectDirectory,
            List<String> dataFiles
    ) {
        if (dataFiles == null || dataFiles.isEmpty()) {
            addTemplateError(
                    result,
                    "Data files",
                    "No data files are configured for this template.",
                    "Configure at least one <file> entry inside <dataFiles>."
            );
            return;
        }

        for (String configuredFile : dataFiles) {
            Path file = resolveInputFile(
                    result,
                    projectDirectory,
                    configuredFile,
                    "Data file"
            );
            if (file == null || !validateReadableFile(
                    result, file, "Data file", "YAML file")) {
                continue;
            }

            try {
                Map<String, Object> loadedData = yamlDataLoader.load(file);

                if (loadedData.size() != 1) {
                    throw new IllegalArgumentException(
                            "YAML loader must return exactly one template data entry."
                    );
                }

                String templateKey = loadedData.keySet().iterator().next();

                if (result.getDataModel().containsKey(templateKey)) {
                    addTemplateError(
                            result,
                            "Data file",
                            "Duplicate template-key '" + templateKey + "' in YAML file: " + file,
                            "Use a unique template-key for each YAML data file used by this template."
                    );
                    continue;
                }

                result.getDataModel().putAll(loadedData);

                addTemplateInfo(
                        result,
                        "Data file '" + templateKey + "'",
                        "YAML data loaded successfully: " + file
                );
            } catch (Exception exception) {
                addTemplateError(
                        result,
                        "Data file",
                        "Failed to load YAML file '" + file + "': " + exception.getMessage(),
                        "Check the YAML syntax, ensure the root is a mapping, and provide a non-empty template-key."
                );
            }
        }
    }

    private boolean validateReadableFile(
            ValidationResult.TemplateResult result,
            Path file,
            String context,
            String description
    ) {
        if (!Files.exists(file)) {
            addTemplateError(
                    result,
                    context,
                    description + " does not exist: " + file,
                    "Check the configured file path."
            );
            return false;
        }

        if (!Files.isRegularFile(file)) {
            addTemplateError(
                    result,
                    context,
                    "Configured path is not a regular file: " + file,
                    "Configure the path to point to an existing file."
            );
            return false;
        }

        if (!Files.isReadable(file)) {
            addTemplateError(
                    result,
                    context,
                    description + " is not readable: " + file,
                    "Check file permissions."
            );
            return false;
        }

        addTemplateInfo(result, context, "File is valid: " + file);
        return true;
    }

    private Path resolveInputFile(
            ValidationResult.TemplateResult result,
            Path projectDirectory,
            String configuredPath,
            String context
    ) {
        String value = trimToNull(configuredPath);
        if (value == null) {
            addTemplateError(
                    result,
                    context,
                    "File path is missing or empty.",
                    "Provide a valid relative or absolute file path."
            );
            return null;
        }

        try {
            Path path = Path.of(value);
            Path resolved = path.isAbsolute()
                    ? path.normalize()
                    : projectDirectory.resolve(path).normalize();

            addTemplateInfo(
                    result,
                    context,
                    "Resolved path: " + resolved.toAbsolutePath().normalize()
            );
            return resolved.toAbsolutePath().normalize();
        } catch (Exception exception) {
            addTemplateError(
                    result,
                    context,
                    "Invalid file path: " + value,
                    "Provide a valid relative or absolute file path."
            );
            return null;
        }
    }

    private void validateOutputFile(
            ValidationResult.TemplateResult result,
            Path outputDirectory,
            String configuredOutput,
            Set<Path> outputFiles
    ) {
        String value = trimToNull(configuredOutput);
        if (value == null) {
            addTemplateError(
                    result,
                    "Output file",
                    "Output path is missing or empty.",
                    "Provide a relative <output> path."
            );
            return;
        }

        try {
            Path relative = Path.of(value);
            if (relative.isAbsolute()) {
                addTemplateError(
                        result,
                        "Output file",
                        "Absolute output paths are not allowed: " + value,
                        "Use a path relative to <outputDirectory>."
                );
                return;
            }

            Path output = outputDirectory.resolve(relative).toAbsolutePath().normalize();
            if (!output.startsWith(outputDirectory)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Output path escapes the configured output directory: " + output,
                        "Use a relative path inside <outputDirectory>."
                );
                return;
            }

            result.setOutputFile(output);

            if (Files.exists(output) && !Files.isRegularFile(output)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Existing output path is not a regular file: " + output,
                        "Choose an output path that points to a file."
                );
                return;
            }

            if (Files.exists(output) && !Files.isWritable(output)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Existing output file is not writable: " + output,
                        "Check file permissions or choose another output path."
                );
                return;
            }

            Path parent = output.getParent();
            Path existing = parent;
            while (existing != null && !Files.exists(existing)) {
                existing = existing.getParent();
            }
            if (existing == null || !Files.isDirectory(existing) || !Files.isWritable(existing)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Output parent directory is missing, invalid, or not writable: " + parent,
                        "Choose an output path under a writable directory."
                );
                return;
            }

            if (!outputFiles.add(output)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Multiple templates use the same output path: " + output,
                        "Configure a unique <output> path for each template."
                );
                return;
            }

            addTemplateInfo(result, "Output file", "Output path is valid: " + output);
        } catch (Exception exception) {
            addTemplateError(
                    result,
                    "Output file",
                    "Invalid output path: " + value,
                    "Provide a valid relative <output> path."
            );
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void addGlobalError(ValidationResult result, String context, String message, String suggestion) {
        result.getGlobalIssues().add(new ValidationIssue(
                ValidationIssue.Severity.ERROR, context, message, suggestion));
    }

    private void addGlobalInfo(ValidationResult result, String context, String message) {
        result.getGlobalIssues().add(new ValidationIssue(
                ValidationIssue.Severity.INFO, context, message, null));
    }

    private void addTemplateError(
            ValidationResult.TemplateResult result,
            String context,
            String message,
            String suggestion
    ) {
        result.getIssues().add(new ValidationIssue(
                ValidationIssue.Severity.ERROR, context, message, suggestion));
    }

    private void addTemplateInfo(
            ValidationResult.TemplateResult result,
            String context,
            String message
    ) {
        result.getIssues().add(new ValidationIssue(
                ValidationIssue.Severity.INFO, context, message, null));
    }
}