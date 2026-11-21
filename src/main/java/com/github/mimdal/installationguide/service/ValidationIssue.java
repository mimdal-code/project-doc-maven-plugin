package com.github.mimdal.installationguide.service;

public class ValidationIssue {

    public enum Severity {
        INFO,
        WARNING,
        ERROR
    }

    private final Severity severity;
    private final String context;
    private final String message;
    private final String suggestion;

    public ValidationIssue(
            Severity severity,
            String context,
            String message,
            String suggestion
    ) {
        this.severity = severity;
        this.context = context;
        this.message = message;
        this.suggestion = suggestion;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getContext() {
        return context;
    }

    public String getMessage() {
        return message;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
