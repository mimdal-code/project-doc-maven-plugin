package com.github.mimdal.installationguide.mojo;

import com.github.mimdal.installationguide.service.ScaffoldGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

@Mojo(name = "scaffold", threadSafe = true)
public class ScaffoldMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File projectDirectory;

    @Override
    public void execute() throws MojoExecutionException {
        if (projectDirectory == null) {
            throw new MojoExecutionException(
                    "Maven project directory is unavailable."
            );
        }

        try {
            ScaffoldGenerator.Result result = new ScaffoldGenerator().generate(
                    projectDirectory.toPath()
            );

            getLog().info("Project documentation scaffold");
            getLog().info("  Directory: " + result.getDirectory());
            getLog().info("  Created:   " + result.getCreated() + " file(s)");
            getLog().info("  Skipped:   " + result.getSkipped()
                    + " existing file(s) (not overwritten)");
            getLog().info("Scaffold generation completed.");
        } catch (Exception exception) {
            throw new MojoExecutionException(
                    "Failed to generate project documentation scaffold.",
                    exception
            );
        }
    }
}
