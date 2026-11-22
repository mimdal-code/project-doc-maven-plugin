package com.github.mimdal.installationguide.service;

import com.github.mimdal.installationguide.model.TemplateConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ConfigurationValidatorTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldResolveRelativeTemplateAndYamlPathsFromProjectDirectory() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path template = project.resolve("project-doc/template/guide.tmpl");
        Path yaml = project.resolve("project-doc/data/main.yaml");

        Files.createDirectories(template.getParent());
        Files.createDirectories(yaml.getParent());

        Files.writeString(
                template,
                "Hello ${main.message}!",
                StandardCharsets.UTF_8
        );
        Files.writeString(
                yaml,
                "template-key: main\nmessage: world\n",
                StandardCharsets.UTF_8
        );

        ValidationResult result = validator().validate(
                project,
                project.resolve("target/docs"),
                Collections.singletonList(configuration(
                        "project-doc/template/guide.tmpl",
                        "project-doc/data/main.yaml"
                ))
        );

        assertFalse(result.hasErrors(), describe(result));
        assertEquals(
                template.toAbsolutePath().normalize(),
                result.getTemplates().get(0).getTemplateFile()
        );

        assertEquals(
                "world",
                ((java.util.Map<?, ?>) result.getTemplates()
                        .get(0).getDataModel()
                        .get("main"))
                        .get("message")
        );
    }

    @Test
    void shouldAcceptAbsoluteTemplateAndYamlPathsOutsideProjectDirectory() throws Exception {
        Path project = Files.createDirectories(temporaryDirectory.resolve("project"));
        Path external = Files.createDirectories(temporaryDirectory.resolve("external"));
        Path template = external.resolve("guide.tmpl");
        Path yaml = external.resolve("main.yaml");

        Files.writeString(
                template,
                "Hello ${main.message}!",
                StandardCharsets.UTF_8
        );
        Files.writeString(
                yaml,
                "template-key: main\nmessage: world\n",
                StandardCharsets.UTF_8
        );

        ValidationResult result = validator().validate(
                project,
                project.resolve("target/docs"),
                Collections.singletonList(configuration(
                        template.toString(),
                        yaml.toString()
                ))
        );

        assertFalse(result.hasErrors(), describe(result));
        assertEquals(
                template.toAbsolutePath().normalize(),
                result.getTemplates().get(0).getTemplateFile()
        );
    }

    private ConfigurationValidator validator() {
        return new ConfigurationValidator(new YamlDataLoader());
    }

    private TemplateConfig configuration(String templatePath, String yamlPath) {
        TemplateConfig template = new TemplateConfig();
        template.setName("guide");
        template.setFile(templatePath);
        template.setOutput("guide.html");
        template.setDataFiles(Collections.singletonList(yamlPath));
        return template;
    }

    private String describe(ValidationResult result) {
        StringBuilder message = new StringBuilder();

        for (ValidationIssue issue : result.getGlobalIssues()) {
            message.append(issue.getMessage()).append('\n');
        }

        for (ValidationResult.TemplateResult template : result.getTemplates()) {
            for (ValidationIssue issue : template.getIssues()) {
                message.append(issue.getMessage()).append('\n');
            }
        }

        return message.toString();
    }
}