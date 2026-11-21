package com.github.mimdal.installationguide.service;

import com.github.mimdal.installationguide.model.TemplateConfig;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidationResult {

    private final List<ValidationIssue> globalIssues = new ArrayList<>();
    private final List<TemplateResult> templates = new ArrayList<>();

    public List<ValidationIssue> getGlobalIssues() {
        return globalIssues;
    }

    public List<TemplateResult> getTemplates() {
        return templates;
    }

    public boolean hasGlobalErrors() {
        return hasErrors(globalIssues);
    }

    public boolean hasErrors() {
        if (hasErrors(globalIssues)) {
            return true;
        }

        for (TemplateResult template : templates) {
            if (template.hasErrors()) {
                return true;
            }
        }

        return false;
    }

    private boolean hasErrors(List<ValidationIssue> issues) {
        return issues.stream().anyMatch(
                issue -> issue.getSeverity() == ValidationIssue.Severity.ERROR
        );
    }

    public static class TemplateResult {

        private final TemplateConfig configuration;
        private final String name;
        private Path templateFile;
        private Path outputFile;
        private final Map<String, Object> dataModel = new HashMap<>();
        private final List<ValidationIssue> issues = new ArrayList<>();

        public TemplateResult(TemplateConfig configuration, String name) {
            this.configuration = configuration;
            this.name = name;
        }

        public TemplateConfig getConfiguration() {
            return configuration;
        }

        public String getName() {
            return name;
        }

        public Path getTemplateFile() {
            return templateFile;
        }

        public void setTemplateFile(Path templateFile) {
            this.templateFile = templateFile;
        }

        public Path getOutputFile() {
            return outputFile;
        }

        public void setOutputFile(Path outputFile) {
            this.outputFile = outputFile;
        }

        public Map<String, Object> getDataModel() {
            return dataModel;
        }

        public List<ValidationIssue> getIssues() {
            return issues;
        }

        public boolean hasErrors() {
            return issues.stream().anyMatch(
                    issue -> issue.getSeverity() == ValidationIssue.Severity.ERROR
            );
        }

        public boolean isValid() {
            return !hasErrors();
        }
    }
}
