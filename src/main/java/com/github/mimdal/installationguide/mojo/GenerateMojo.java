package com.github.mimdal.installationguide.mojo;

import com.github.mimdal.installationguide.model.TemplateConfig;
import com.github.mimdal.installationguide.service.*;
import freemarker.template.TemplateException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.List;

//noinspection unused
@Mojo(
        name = "generate",
        threadSafe = true
)
public class GenerateMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File projectDirectory;

    @Parameter(
            defaultValue = "${project.build.directory}/project-doc",
            required = true
    )
    private File outputDirectory;

    @Parameter
    private List<TemplateConfig> templates;

    @Parameter(
            property = "installationGuide.skip",
            defaultValue = "false"
    )
    private boolean skip;

    @Parameter(
            property = "installationGuide.failOnError",
            defaultValue = "false"
    )
    private boolean failOnError;

    private final YamlDataLoader yamlDataLoader = new YamlDataLoader();

    private final TemplateRenderer templateRenderer = new TemplateRenderer();

    private final ConfigurationValidator configurationValidator =
            new ConfigurationValidator(yamlDataLoader);

    @Override
    public void execute() throws MojoExecutionException {

        if (skip) {
            getLog().info("Installation guide generation skipped.");
            return;
        }

        getLog().info("Installation Guide Maven Plugin");
        getLog().info("Validating configuration...");

        ValidationResult validationResult = configurationValidator.validate(
                projectDirectory == null ? null : projectDirectory.toPath(),
                outputDirectory == null ? null : outputDirectory.toPath(),
                templates
        );

        reportValidation(validationResult);

        if (validationResult.hasErrors() && failOnError) {
            throw new MojoExecutionException(
                    "Installation guide configuration validation failed. "
                            + "See the validation report above for details."
            );
        }

        if (validationResult.hasGlobalErrors()) {
            getLog().warn(
                    "Generation skipped because global configuration is invalid."
            );
            return;
        }

        int generated = 0;
        int skippedTemplates = 0;
        int failed = 0;

        getLog().info("Starting documentation generation...");

        for (ValidationResult.TemplateResult templateResult
                : validationResult.getTemplates()) {

            if (!templateResult.isValid()) {
                skippedTemplates++;

                getLog().warn(
                        "Skipping template '" + templateResult.getName()
                                + "' because its configuration is invalid."
                );

                continue;
            }

            try {
                processTemplate(templateResult);
                generated++;

            } catch (Exception exception) {
                failed++;

                reportRenderingFailure(templateResult, exception);

                if (failOnError) {
                    throw new MojoExecutionException(
                            "Failed to generate template '"
                                    + templateResult.getName()
                                    + "'. See the detailed error report above.",
                            exception
                    );
                }
            }
        }

        getLog().info("");
        getLog().info("Installation guide generation summary:");
        getLog().info("  Generated templates: " + generated);
        getLog().info("  Skipped templates:   " + skippedTemplates);
        getLog().info("  Failed templates:    " + failed);

        if (validationResult.hasErrors()) {
            getLog().warn(
                    "Generation completed with configuration errors. "
                            + "Review the validation report and correct the configuration."
            );
        } else if (failed > 0) {
            getLog().warn("Generation completed with rendering errors.");
        } else {
            getLog().info(
                    "Installation guide generation completed successfully."
            );
        }
    }

    private void reportValidation(ValidationResult result) {

        getLog().info("");
        getLog().info("Configuration validation report");
        getLog().info("--------------------------------");

        if (outputDirectory != null) {
            getLog().info("Output directory:");
            getLog().info("  Configured: " + outputDirectory);
            getLog().info(
                    "  Resolved:   "
                            + outputDirectory.toPath().toAbsolutePath().normalize()
            );
        }

        for (ValidationIssue issue : result.getGlobalIssues()) {
            reportIssue(issue);
        }

        for (ValidationResult.TemplateResult template : result.getTemplates()) {

            getLog().info("");
            getLog().info("Template: " + template.getName());

            if (template.getConfiguration() != null) {
                getLog().info(
                        "  Configured file: "
                                + template.getConfiguration().getFile()
                );
                getLog().info(
                        "  Configured output: "
                                + template.getConfiguration().getOutput()
                );
            }

            if (template.getTemplateFile() != null) {
                getLog().info(
                        "  Resolved file: " + template.getTemplateFile()
                );
            }

            if (template.getOutputFile() != null) {
                getLog().info(
                        "  Resolved output: " + template.getOutputFile()
                );
            }

            for (ValidationIssue issue : template.getIssues()) {
                reportIssue(issue);
            }

            getLog().info(
                    "  Status: " + (template.isValid() ? "VALID" : "INVALID")
            );
        }

        getLog().info("");
        getLog().info(
                "Validation result: "
                        + (result.hasErrors() ? "ERRORS FOUND" : "VALID")
        );
        getLog().info("--------------------------------");
        getLog().info("");
    }

    private void reportIssue(ValidationIssue issue) {

        String prefix;

        switch (issue.getSeverity()) {
            case ERROR:
                prefix = "[ERROR]";
                break;

            case WARNING:
                prefix = "[WARNING]";
                break;

            default:
                prefix = "[INFO]";
        }

        String message = prefix + " " + issue.getContext()
                + ": " + issue.getMessage();

        if (issue.getSeverity() == ValidationIssue.Severity.ERROR) {
            getLog().error(message);
        } else if (issue.getSeverity() == ValidationIssue.Severity.WARNING) {
            getLog().warn(message);
        } else {
            getLog().info(message);
        }

        if (issue.getSuggestion() != null
                && !issue.getSuggestion().trim().isEmpty()) {

            getLog().info("         How to fix: " + issue.getSuggestion());
        }
    }

    private void processTemplate(
            ValidationResult.TemplateResult templateResult
    ) throws Exception {

        getLog().info("");
        getLog().info(
                "Generating template '" + templateResult.getName() + "'..."
        );
        getLog().info("  Template: " + templateResult.getTemplateFile());
        getLog().info("  Output:   " + templateResult.getOutputFile());

        templateRenderer.render(
                templateResult.getTemplateFile(),
                templateResult.getOutputFile(),
                templateResult.getDataModel()
        );

        getLog().info(
                "Generated: " + templateResult.getOutputFile()
        );
    }

    private void reportRenderingFailure(
            ValidationResult.TemplateResult templateResult,
            Exception exception
    ) {

        getLog().error("");
        getLog().error("Documentation template generation failed");
        getLog().error("----------------------------------------");
        getLog().error("Template name: " + templateResult.getName());
        getLog().error("Template file: " + templateResult.getTemplateFile());
        getLog().error("Output file:   " + templateResult.getOutputFile());
        getLog().error("Failure type:  " + exception.getClass().getName());

        String message = exception.getMessage();

        if (message != null && !message.trim().isEmpty()) {
            getLog().error("Message:       " + message);
        }

        if (exception instanceof TemplateException) {
            reportFreeMarkerException((TemplateException) exception);
        }

        Throwable rootCause = getRootCause(exception);

        if (rootCause != exception) {
            getLog().error(
                    "Root cause:    " + rootCause.getClass().getName()
                            + ": " + rootCause.getMessage()
            );
        }

        getLog().error("----------------------------------------");
        getLog().error(
                "For the complete Java stack trace, run Maven with -e."
        );

        getLog().debug("Full rendering exception:", exception);
    }

    private void reportFreeMarkerException(TemplateException exception) {

        String templateSourceName = exception.getTemplateSourceName();

        if (templateSourceName != null) {
            getLog().error(
                    "FreeMarker template: " + templateSourceName
            );
        }

        Integer lineNumber = exception.getLineNumber();
        Integer columnNumber = exception.getColumnNumber();

        if (lineNumber != null && lineNumber > 0) {

            String location = "Template location:   line " + lineNumber;

            if (columnNumber != null && columnNumber > 0) {
                location += ", column " + columnNumber;
            }

            getLog().error(location);
        }

        String expression = exception.getBlamedExpressionString();

        if (expression != null && !expression.trim().isEmpty()) {
            getLog().error("Failing expression:  " + expression);
        }

        String instructionStack = exception.getFTLInstructionStack();

        if (instructionStack != null
                && !instructionStack.trim().isEmpty()) {

            getLog().error("FreeMarker instruction stack:");
            getLog().error(instructionStack);
        }
    }

    private Throwable getRootCause(Throwable throwable) {

        Throwable current = throwable;

        while (current.getCause() != null
                && current.getCause() != current) {
            current = current.getCause();
        }

        return current;
    }
}
