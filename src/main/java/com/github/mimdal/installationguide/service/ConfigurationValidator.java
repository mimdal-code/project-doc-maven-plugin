package com.github.mimdal.installationguide.service;

import com.github.mimdal.installationguide.model.DataFileConfig;
import com.github.mimdal.installationguide.model.TemplateConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationValidator {

    private final YamlDataLoader yamlDataLoader;
    public ConfigurationValidator(YamlDataLoader yamlDataLoader) {
        this.yamlDataLoader = yamlDataLoader;
    }

    public ValidationResult validate(
            Path sourceDirectory,
            Path outputDirectory,
            List<TemplateConfig> templates
    ) {

        ValidationResult result = new ValidationResult();

        Path resolvedSourceDirectory = validateSourceDirectory(
                sourceDirectory,
                result
        );

        Path resolvedOutputDirectory = validateOutputDirectory(
                outputDirectory,
                result
        );

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

                ValidationResult.TemplateResult invalidTemplate =
                        new ValidationResult.TemplateResult(
                                null,
                                "<unnamed>"
                        );

                addTemplateError(
                        invalidTemplate,
                        "Template configuration",
                        "A null template entry was found.",
                        "Remove the empty <template> entry or configure it with valid values."
                );

                result.getTemplates().add(invalidTemplate);
                continue;
            }

            String templateName = trimToNull(configuration.getName());

            if (templateName == null) {
                templateName = "<unnamed>";
            }

            ValidationResult.TemplateResult templateResult =
                    new ValidationResult.TemplateResult(
                            configuration,
                            templateName
                    );

            result.getTemplates().add(templateResult);

            validateTemplateName(templateResult, configuration);

            validateTemplateFile(
                    templateResult,
                    resolvedSourceDirectory,
                    configuration.getFile()
            );

            validateDataFiles(
                    templateResult,
                    resolvedSourceDirectory,
                    configuration.getDataFiles()
            );

            validateOutputFile(
                    templateResult,
                    resolvedOutputDirectory,
                    configuration.getOutput(),
                    outputFiles
            );
        }

        return result;
    }

    private Path validateSourceDirectory(
            Path sourceDirectory,
            ValidationResult result
    ) {

        if (sourceDirectory == null) {
            addGlobalError(
                    result,
                    "Source directory",
                    "Source directory is not configured.",
                    "Configure a valid <sourceDirectory>."
            );
            return null;
        }

        Path resolved = sourceDirectory.toAbsolutePath().normalize();

        if (!Files.exists(resolved)) {
            addGlobalError(
                    result,
                    "Source directory",
                    "Source directory does not exist: " + resolved,
                    "Create the directory or correct the <sourceDirectory> configuration."
            );
            return null;
        }

        if (!Files.isDirectory(resolved)) {
            addGlobalError(
                    result,
                    "Source directory",
                    "Source path is not a directory: " + resolved,
                    "Configure <sourceDirectory> to point to an existing directory."
            );
            return null;
        }

        if (!Files.isReadable(resolved)) {
            addGlobalError(
                    result,
                    "Source directory",
                    "Source directory is not readable: " + resolved,
                    "Check directory permissions."
            );
            return null;
        }

        addGlobalInfo(
                result,
                "Source directory",
                "Source directory is valid: " + resolved
        );

        return resolved;
    }

    private Path validateOutputDirectory(
            Path outputDirectory,
            ValidationResult result
    ) {

        if (outputDirectory == null) {
            addGlobalError(
                    result,
                    "Output directory",
                    "Output directory is not configured.",
                    "Configure a valid <outputDirectory>."
            );
            return null;
        }

        Path resolved = outputDirectory.toAbsolutePath().normalize();

        if (Files.exists(resolved)) {

            if (!Files.isDirectory(resolved)) {
                addGlobalError(
                        result,
                        "Output directory",
                        "Output path exists but is not a directory: " + resolved,
                        "Configure <outputDirectory> to point to a directory."
                );
                return null;
            }

            if (!Files.isWritable(resolved)) {
                addGlobalError(
                        result,
                        "Output directory",
                        "Output directory is not writable: " + resolved,
                        "Check directory permissions or choose another <outputDirectory>."
                );
                return null;
            }

        } else {

            Path existingAncestor = resolved;

            while (existingAncestor != null
                    && !Files.exists(existingAncestor)) {
                existingAncestor = existingAncestor.getParent();
            }

            if (existingAncestor == null) {
                addGlobalError(
                        result,
                        "Output directory",
                        "No existing parent directory was found for: " + resolved,
                        "Choose an output path under an existing writable directory."
                );
                return null;
            }

            if (!Files.isDirectory(existingAncestor)) {
                addGlobalError(
                        result,
                        "Output directory",
                        "The nearest existing parent is not a directory: "
                                + existingAncestor,
                        "Choose an output path under an existing directory."
                );
                return null;
            }

            if (!Files.isWritable(existingAncestor)) {
                addGlobalError(
                        result,
                        "Output directory",
                        "The nearest existing parent is not writable: "
                                + existingAncestor,
                        "Check directory permissions or choose another <outputDirectory>."
                );
                return null;
            }
        }

        addGlobalInfo(
                result,
                "Output directory",
                "Output directory is valid: " + resolved
        );

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
                    "Provide a unique descriptive <name> for this template."
            );
        }
    }

    private void validateTemplateFile(
            ValidationResult.TemplateResult result,
            Path sourceDirectory,
            String configuredFile
    ) {

        Path templateFile = resolveInputFile(
                result,
                sourceDirectory,
                configuredFile,
                "Template file",
                "Provide a valid relative template <file> path."
        );

        if (templateFile == null) {
            return;
        }

        result.setTemplateFile(templateFile);

        if (!Files.exists(templateFile)) {
            addTemplateError(
                    result,
                    "Template file",
                    "Template file does not exist: " + templateFile,
                    buildMissingFileSuggestion(sourceDirectory, configuredFile)
            );
            return;
        }

        if (!Files.isRegularFile(templateFile)) {
            addTemplateError(
                    result,
                    "Template file",
                    "Template path is not a regular file: " + templateFile,
                    "Configure <file> to point to an existing template file."
            );
            return;
        }

        if (!Files.isReadable(templateFile)) {
            addTemplateError(
                    result,
                    "Template file",
                    "Template file is not readable: " + templateFile,
                    "Check file permissions."
            );
            return;
        }

        addTemplateInfo(
                result,
                "Template file",
                "Template file is valid: " + templateFile
        );
    }

    private void validateDataFiles(
            ValidationResult.TemplateResult result,
            Path sourceDirectory,
            List<DataFileConfig> dataFiles
    ) {

        if (dataFiles == null || dataFiles.isEmpty()) {
            addTemplateError(
                    result,
                    "Data files",
                    "No data files are configured for this template.",
                    "Configure at least one <dataFile> with a <name> and <file>."
            );
            return;
        }

        Set<String> aliases = new HashSet<>();

        for (DataFileConfig dataFile : dataFiles) {

            if (dataFile == null) {
                addTemplateError(
                        result,
                        "Data file configuration",
                        "A null data file entry was found.",
                        "Remove the empty <dataFile> entry or configure it with a valid <name> and <file>."
                );
                continue;
            }

            String alias = trimToNull(dataFile.getName());

            if (alias == null) {
                addTemplateError(
                        result,
                        "Data file alias",
                        "Data file alias is missing or empty.",
                        "Provide a unique <name> for this data file."
                );
                continue;
            }

            if (!aliases.add(alias)) {
                addTemplateError(
                        result,
                        "Data file alias",
                        "Duplicate data file alias: " + alias,
                        "Use a unique <name> for each data file within this template."
                );
                continue;
            }

            Path dataFilePath = resolveInputFile(
                    result,
                    sourceDirectory,
                    dataFile.getFile(),
                    "Data file '" + alias + "'",
                    "Configure a valid relative <file> path for data file '" + alias + "'."
            );

            if (dataFilePath == null) {
                continue;
            }

            if (!Files.exists(dataFilePath)) {
                addTemplateError(
                        result,
                        "Data file '" + alias + "'",
                        "Data file does not exist: " + dataFilePath,
                        "Check the configured <file> path for data file '" + alias + "'."
                );
                continue;
            }

            if (!Files.isRegularFile(dataFilePath)) {
                addTemplateError(
                        result,
                        "Data file '" + alias + "'",
                        "Data path is not a regular file: " + dataFilePath,
                        "Configure <file> to point to an existing YAML file."
                );
                continue;
            }

            if (!Files.isReadable(dataFilePath)) {
                addTemplateError(
                        result,
                        "Data file '" + alias + "'",
                        "Data file is not readable: " + dataFilePath,
                        "Check file permissions."
                );
                continue;
            }

            try {
                Map<String, Object> data = yamlDataLoader.load(dataFilePath);

                result.getDataModel().put(alias, data);

                addTemplateInfo(
                        result,
                        "Data file '" + alias + "'",
                        "YAML data loaded successfully: " + dataFilePath
                );

            } catch (Exception e) {
                addTemplateError(
                        result,
                        "Data file '" + alias + "'",
                        "Failed to load YAML file '"
                                + dataFilePath
                                + "': "
                                + e.getMessage(),
                        "Check the YAML syntax and ensure that the YAML root is a mapping."
                );
            }
        }
    }

    private void validateOutputFile(
            ValidationResult.TemplateResult result,
            Path outputDirectory,
            String configuredOutput,
            Set<Path> outputFiles
    ) {

        String outputValue = trimToNull(configuredOutput);

        if (outputValue == null) {
            addTemplateError(
                    result,
                    "Output file",
                    "Output path is missing or empty.",
                    "Provide a relative <output> path for this template."
            );
            return;
        }

        Path relativePath;

        try {
            relativePath = Path.of(outputValue);

        } catch (Exception e) {
            addTemplateError(
                    result,
                    "Output file",
                    "Invalid output path: " + outputValue,
                    "Provide a valid relative <output> path."
            );
            return;
        }

        if (relativePath.isAbsolute()) {
            addTemplateError(
                    result,
                    "Output file",
                    "Absolute output paths are not allowed: " + outputValue,
                    "Use a path relative to <outputDirectory>."
            );
            return;
        }

        Path output = outputDirectory.resolve(relativePath)
                .toAbsolutePath()
                .normalize();

        if (!output.startsWith(outputDirectory)) {
            addTemplateError(
                    result,
                    "Output file",
                    "Output path escapes the configured output directory: "
                            + output,
                    "Use a relative <output> path that remains inside <outputDirectory>."
            );
            return;
        }

        result.setOutputFile(output);

        if (Files.exists(output)) {

            if (!Files.isRegularFile(output)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Existing output path is not a regular file: " + output,
                        "Choose an <output> path that points to a file."
                );
                return;
            }

            if (!Files.isWritable(output)) {
                addTemplateError(
                        result,
                        "Output file",
                        "Existing output file is not writable: " + output,
                        "Check file permissions or choose another <output> path."
                );
                return;
            }
        }

        Path parent = output.getParent();

        if (parent == null) {
            addTemplateError(
                    result,
                    "Output file",
                    "Could not determine the parent directory for: " + output,
                    "Provide a valid relative <output> path."
            );
            return;
        }

        Path existingParent = parent;

        while (existingParent != null && !Files.exists(existingParent)) {
            existingParent = existingParent.getParent();
        }

        if (existingParent == null
                || !Files.isDirectory(existingParent)
                || !Files.isWritable(existingParent)) {

            addTemplateError(
                    result,
                    "Output file",
                    "Output parent directory is missing, invalid, or not writable: "
                            + parent,
                    "Check directory permissions or choose an output path under a writable directory."
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

        addTemplateInfo(
                result,
                "Output file",
                "Output path is valid: " + output
        );
    }

    private Path resolveInputFile(
            ValidationResult.TemplateResult result,
            Path sourceDirectory,
            String configuredPath,
            String context,
            String suggestion
    ) {

        String value = trimToNull(configuredPath);

        if (value == null) {
            addTemplateError(
                    result,
                    context,
                    "File path is missing or empty.",
                    suggestion
            );
            return null;
        }

        Path relativePath;

        try {
            relativePath = Path.of(value);

        } catch (Exception e) {
            addTemplateError(
                    result,
                    context,
                    "Invalid file path: " + value,
                    suggestion
            );
            return null;
        }

        if (relativePath.isAbsolute()) {
            addTemplateError(
                    result,
                    context,
                    "Absolute file paths are not allowed: " + value,
                    "Use a path relative to the configured source directory."
            );
            return null;
        }

        Path resolved = sourceDirectory.resolve(relativePath)
                .toAbsolutePath()
                .normalize();

        if (!resolved.startsWith(sourceDirectory)) {
            addTemplateError(
                    result,
                    context,
                    "File path escapes the configured source directory: "
                            + resolved,
                    "Use a relative path that remains inside <sourceDirectory>."
            );
            return null;
        }

        if (Files.exists(resolved)) {
            try {
                Path realSource = sourceDirectory.toRealPath();
                Path realFile = resolved.toRealPath();

                if (!realFile.startsWith(realSource)) {
                    addTemplateError(
                            result,
                            context,
                            "File resolves outside the configured source directory: "
                                    + realFile,
                            "Do not use symbolic links that point outside <sourceDirectory>."
                    );
                    return null;
                }

            } catch (IOException e) {
                addTemplateError(
                        result,
                        context,
                        "Could not resolve file path: " + resolved
                                + ". " + e.getMessage(),
                        "Check the file and directory permissions."
                );
                return null;
            }
        }

        return resolved;
    }

 private String buildMissingFileSuggestion(
        Path sourceDirectory,
        String configuredFile
) {

    List<String> availableFiles = findAvailableFiles(sourceDirectory);

    StringBuilder suggestion = new StringBuilder();

    suggestion.append("Check the configured path '")
            .append(configuredFile)
            .append("'. Ensure that the file exists under ")
            .append(sourceDirectory)
            .append(".");

    if (!availableFiles.isEmpty()) {
        suggestion.append(" Available files include: ")
                .append(String.join(", ", availableFiles))
                .append(".");
    }

    return suggestion.toString();
}

    private List<String> findAvailableFiles(Path sourceDirectory) {

        if (sourceDirectory == null
                || !Files.isDirectory(sourceDirectory)) {
            return new ArrayList<>();
        }

        try (Stream<Path> stream = Files.walk(sourceDirectory, 3)) {

            return stream
                    .filter(Files::isRegularFile)
                    .map(sourceDirectory::relativize)
                    .map(Path::toString)
                    .sorted()
                    .limit(30)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private String trimToNull(String value) {

        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty() ? null : trimmed;
    }

    private void addGlobalError(
            ValidationResult result,
            String context,
            String message,
            String suggestion
    ) {

        result.getGlobalIssues().add(
                new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        context,
                        message,
                        suggestion
                )
        );
    }

    private void addGlobalInfo(
            ValidationResult result,
            String context,
            String message
    ) {

        result.getGlobalIssues().add(
                new ValidationIssue(
                        ValidationIssue.Severity.INFO,
                        context,
                        message,
                        null
                )
        );
    }

    private void addTemplateError(
            ValidationResult.TemplateResult result,
            String context,
            String message,
            String suggestion
    ) {

        result.getIssues().add(
                new ValidationIssue(
                        ValidationIssue.Severity.ERROR,
                        context,
                        message,
                        suggestion
                )
        );
    }

    private void addTemplateInfo(
            ValidationResult.TemplateResult result,
            String context,
            String message
    ) {

        result.getIssues().add(
                new ValidationIssue(
                        ValidationIssue.Severity.INFO,
                        context,
                        message,
                        null
                )
        );
    }
}
