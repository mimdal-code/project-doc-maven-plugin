project-doc-maven-plugin
==============================

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

# About 

A configurable Maven plugin for generating project documentation during the build lifecycle.

Define documentation content using YAML files and control its presentation using FreeMarker templates. Generate consistent, maintainable documentation in multiple formats without manually maintaining separate documents.

---

## Documentation

- [Developer Guide](developer-guide.md) — Learn how to create templates, define YAML data sources, and generate HTML or Markdown documentation.

---

## Features

- **Build-time documentation generation:** Generate project documentation as part of the Maven build lifecycle.
- **Multiple output formats:** Generate Markdown, HTML, and other formats supported by your templates.
- **YAML-based content:** Keep documentation content separate from presentation.
- **Customizable templates:** Use FreeMarker to control the structure and appearance of generated documents.
- **Multiple data sources:** Load multiple YAML files into a single template.
- **Reusable data:** Share the same YAML sources between different output formats.
- **Structured configuration documentation:** Organize application properties into sections and tables.
- **Vault documentation:** Document sensitive configuration keys without exposing their actual values.
- **Maven integration:** Integrate documentation generation into your existing build process.

## Requirements

- Java 17 or later
- Apache Maven

## Installation

Add the plugin to your project's `pom.xml`:

```
<build>
    <plugins>
        <plugin>
            <groupId>com.github.mimdal</groupId>
            <artifactId>project-doc-maven-plugin</artifactId>
            <version>${plugin.version}</version>
        </plugin>
    </plugins>
</build>
```

Replace `${plugin.version}` with the desired released version.

The plugin is published to Maven Central and can be resolved using the standard Maven repository configuration.

[Maven Central – project-doc-maven-plugin](https://central.sonatype.com/artifact/com.github.mimdal/project-doc-maven-plugin)

## Usage

### Automatic generation

The plugin is configured to execute during the Maven `package` phase by default.

Simply run:

```
mvn package
```

Documentation is generated as part of the normal Maven build.

### Direct execution

You can also invoke the plugin goal directly:

```
mvn project-doc:generate
```

This is useful when you want to generate documentation without running the complete package lifecycle.

## Configuration

The plugin provides configuration for source resources, output locations, templates, and YAML data files.

### Example configuration

The following example demonstrates a configuration with two output formats: Markdown and HTML.

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>

    <configuration>
        <!-- Directory containing YAML files and templates -->
        <sourceDirectory>
            ${project.basedir}/src/main/resources/project-doc
        </sourceDirectory>

        <!-- Directory for generated documentation -->
        <outputDirectory>
            ${project.build.directory}/project-doc
        </outputDirectory>

        <templates>
            <!-- Markdown output -->
            <template>
                <name>markdown</name>
                <file>template/index.md.tmpl</file>
                <output>index.md</output>

                <dataFiles>
                    <dataFile>
                        <name>main</name>
                        <file>doc-main.yaml</file>
                    </dataFile>

                    <dataFile>
                        <name>vault</name>
                        <file>doc-vault.yaml</file>
                    </dataFile>

                    <dataFile>
                        <name>properties</name>
                        <file>doc-properties.yaml</file>
                    </dataFile>
                </dataFiles>
            </template>

            <!-- HTML output -->
            <template>
                <name>html</name>
                <file>template/installation-guide.html.tmpl</file>
                <output>installation-guide.html</output>

                <dataFiles>
                    <dataFile>
                        <name>main</name>
                        <file>doc-main.yaml</file>
                    </dataFile>

                    <dataFile>
                        <name>vault</name>
                        <file>doc-vault.yaml</file>
                    </dataFile>

                    <dataFile>
                        <name>properties</name>
                        <file>doc-properties.yaml</file>
                    </dataFile>
                </dataFiles>
            </template>
        </templates>
    </configuration>
</plugin>
```

### Configuration reference

| Configuration        | Description                                              |
| -------------------- | -------------------------------------------------------- |
| `sourceDirectory`    | Base directory containing YAML data files and templates. |
| `outputDirectory`    | Directory where generated documents are written.         |
| `templates`          | Collection of template configurations.                   |
| `template.name`      | Logical name identifying a template configuration.       |
| `template.file`      | Template file path relative to the source directory.     |
| `template.output`    | Output filename for the generated document.              |
| `template.dataFiles` | YAML data files made available to the template.          |
| `dataFile.name`      | Alias used to access the YAML data inside the template.  |
| `dataFile.file`      | YAML file path relative to the source directory.         |

### Source and output directories

The `sourceDirectory` is the base directory used to resolve template and YAML file paths.

The `outputDirectory` is where the generated documentation is written.

For example:

```
<sourceDirectory>
    ${project.basedir}/src/main/resources/project-doc
</sourceDirectory>

<outputDirectory>
    ${project.build.directory}/project-doc
</outputDirectory>
```

### Template configuration

Each template configuration defines one output document.

| Property    | Description                                  |
| ----------- | -------------------------------------------- |
| `name`      | Logical template identifier.                 |
| `file`      | FreeMarker template file.                    |
| `output`    | Generated output filename.                   |
| `dataFiles` | YAML data sources available to the template. |

You can define multiple templates to generate different documents or formats from the same YAML sources.

### YAML data file configuration

Each data file has two properties:

- `name`: The alias used to access the loaded YAML data.
- `file`: The YAML file path relative to the source directory.

For example:

```
<dataFiles>
    <dataFile>
        <name>main</name>
        <file>doc-main.yaml</file>
    </dataFile>

    <dataFile>
        <name>vault</name>
        <file>doc-vault.yaml</file>
    </dataFile>
</dataFiles>
```

These files become available in the template as `main` and `vault`.

## Project structure

A typical project configuration:

```
src/
└── main/
    └── resources/
        └── project-doc/
            ├── doc-main.yaml
            ├── doc-vault.yaml
            ├── doc-properties.yaml
            │
            └── template/
                ├── index.md.tmpl
                └── installation-guide.html.tmpl

target/
└── project-doc/
    ├── index.md
    └── installation-guide.html
```

The source directory contains two types of resources:

1. **YAML files:** Define document content and structured data.
2. **FreeMarker templates:** Define how the content is presented.

The generated documents are written to the configured output directory.

---

## Working with YAML

YAML files define the documentation content independently of the output format.

This separation allows you to maintain the content in one place and reuse it across multiple templates.

The following examples demonstrate one possible documentation structure.

### 1. Main document — `doc-main.yaml`

Contains document metadata and installation environment information.

```
document:
  title: "Project Installation Guide"
  subtitle: "Application configuration and deployment"
  version: "1.0.0"
  author: ""

environment:
  heading: "Installation Environment"

  description: >-
    The application is a Java / Spring Boot microservice
    and does not depend on a specific operating system.

  preparation:
    heading: "Preparation Steps"

    steps:
      - title: "Configure the application"
        command: "cp application.properties.example application.properties"
```

#### Document properties

| Property                          | Description                                  |
| --------------------------------- | -------------------------------------------- |
| `document.title`                  | Document title.                              |
| `document.subtitle`               | Additional document information.             |
| `document.version`                | Document version.                            |
| `document.author`                 | Document author.                             |
| `environment.heading`             | Installation environment heading.            |
| `environment.description`         | Description of the installation environment. |
| `environment.preparation.heading` | Heading for preparation instructions.        |
| `environment.preparation.steps`   | List of preparation steps.                   |
| `steps[].title`                   | Preparation step title.                      |
| `steps[].command`                 | Command associated with the step.            |

Collections can be empty:

```
steps: []
```

Optional properties can also be omitted if the template checks for their existence before accessing them.

### 2. Vault configuration — `doc-vault.yaml`

Contains sensitive configuration keys stored in Vault.

```
vault:
  heading: "Vault Configuration"

  intro: >-
    The following values are stored in Vault and injected
    into the application at runtime:

  groups:
    - heading: "Database"
      items:
        - label: "Database URL"
          key: "db.url"

        - label: "Username"
          key: "db.username"

        - label: "Password"
          key: "db.password"

    - heading: "Authentication"
      items:
        - label: "Client Secret"
          key: "keycloak.client-secret"

      footnote: >-
        Configure the client secret in the Keycloak administration console.
```

#### Vault properties

| Property            | Description                                    |
| ------------------- | ---------------------------------------------- |
| `vault.heading`     | Main Vault section heading.                    |
| `vault.intro`       | Introductory description.                      |
| `vault.groups`      | Collection of configuration groups.            |
| `groups[].heading`  | Group heading.                                 |
| `groups[].items`    | Configuration items belonging to the group.    |
| `items[].label`     | Human-readable configuration label.            |
| `items[].key`       | Actual Vault key.                              |
| `groups[].footnote` | Optional additional information for the group. |

The `footnote` property is optional.

You can use it to provide additional explanations, instructions, or references.

### 3. Application properties — `doc-properties.yaml`

Contains general application configuration properties.

Properties are organized into sections. Each section can be rendered as an independent table.

```
properties:
  heading: "Application Properties"

  description: >-
    General application settings are defined
    in application.properties.

  sections:
    - heading: "Database Configuration"
      items:
        - name: "spring.datasource.url"
          value: ""
          description: "Database connection URL"

        - name: "spring.datasource.username"
          value: ""
          description: "Database username"

    - heading: "Monitoring"
      items:
        - name: "management.tracing.enabled"
          value: "true"
          description: "Enable distributed tracing"
```

#### Properties structure

| Property                 | Description                               |
| ------------------------ | ----------------------------------------- |
| `properties.heading`     | Main configuration section heading.       |
| `properties.description` | General explanation of the configuration. |
| `properties.sections`    | Collection of configuration sections.     |
| `sections[].heading`     | Heading for an individual section.        |
| `sections[].items`       | Properties belonging to the section.      |
| `items[].name`           | Property name.                            |
| `items[].value`          | Property value or example value.          |
| `items[].description`    | Explanation of the property.              |

The `value` field can be empty when the actual value is environment-dependent or should not be disclosed.

---

## Writing FreeMarker templates

The plugin uses [FreeMarker](https://freemarker.apache.org/) as its template engine.

Templates control the document structure, formatting, and presentation. You can create or modify templates to generate documentation that matches your project's requirements.

Template files use the `.tmpl` extension.

### Accessing YAML data

Each YAML file is exposed through its configured alias.

For example, consider this configuration:

```
<dataFile>
    <name>main</name>
    <file>doc-main.yaml</file>
</dataFile>
```

The YAML data becomes available through the `main` variable.

Given this YAML:

```
document:
  title: "Installation Guide"
  version: "1.0.0"
```

You can access its properties using:

```
${main.document.title}
${main.document.version}
```

Similarly, the other aliases are available as:

```
${vault.vault.heading}
${vault.vault.intro}

${properties.properties.heading}
${properties.properties.description}
```

The first part is the configured data alias. The remaining parts follow the YAML structure.

### Accessing nested properties

YAML objects are loaded as nested data structures.

For example:

```
environment:
  preparation:
    heading: "Preparation Steps"
```

Can be accessed as:

```
${main.environment.preparation.heading}
```

### Iterating over collections

Use FreeMarker's `list` directive to iterate over YAML arrays.

YAML:

```
groups:
  - heading: "Database"
    items:
      - label: "Username"
        key: "db.username"

      - label: "Password"
        key: "db.password"
```

Template:

```
<#list vault.vault.groups as group>
    <h2>${group.heading}</h2>

    <#list group.items as item>
        <p>${item.label}: ${item.key}</p>
    </#list>
</#list>
```

### Conditional rendering

Use the `if` directive to render optional sections.

```
<#if main.environment.preparation.steps?has_content>
    <h2>${main.environment.preparation.heading}</h2>

    <#list main.environment.preparation.steps as step>
        <h3>${step.title}</h3>
        <pre><code>${step.command}</code></pre>
    </#list>
</#if>
```

If a YAML property itself is optional, check whether it exists before accessing it:

```
<#if main.environment.prerequisites?? &&
     main.environment.prerequisites?has_content>

    <#list main.environment.prerequisites as item>
        <p>${item.name}</p>
    </#list>

</#if>
```

This prevents errors when the property is absent.

### HTML escaping

When generating HTML, ordinary text values should be HTML-escaped.

Use FreeMarker's `?html` built-in:

```
<h1>${main.document.title?html}</h1>

<p>${main.environment.description?html}</p>
```

For Markdown output, ordinary text generally does not need HTML escaping:

```
# ${main.document.title}

${main.environment.description}
```

The plugin does not automatically configure a FreeMarker HTML output format. Therefore, automatic HTML escaping should not be assumed.

### Rendering HTML stored in YAML

Sometimes a YAML value intentionally contains HTML markup:

```
footnote: >-
  Configure the secret using
  <span class="key">keycloak.client-secret</span>.
```

If the HTML template should preserve that markup, render it directly:

```
<div class="note">
    <p>${group.footnote}</p>
</div>
```

Only use this approach with trusted HTML content. For ordinary text, use `?html`.

### Multiple output formats

You can define multiple templates that use the same YAML sources.

For example:

| Template                       | Output                    |
| ------------------------------ | ------------------------- |
| `index.md.tmpl`                | `index.md`                |
| `installation-guide.html.tmpl` | `installation-guide.html` |

Both templates can access the same data aliases:

```
${main.document.title}
${vault.vault.heading}
${properties.properties.heading}
```

This allows you to maintain one set of documentation content while generating multiple presentation formats.

---

## Generated documentation

With the example configuration, the plugin generates:

```
target/
└── project-doc/
    ├── index.md
    └── installation-guide.html
```

Generated documents can be:

- Published as project documentation.
- Included in deployment packages.
- Used as installation and configuration references.
- Published through GitHub Pages or another documentation hosting service.
- Distributed alongside application releases.

## Best practices

- Keep documentation content in YAML files rather than hardcoding it in templates.
- Separate logically different documentation areas into independent YAML files.
- Use meaningful aliases for YAML data sources.
- Keep templates focused on presentation.
- Organize configuration properties into meaningful sections.
- Use optional-property checks when a YAML property might be absent.
- Escape ordinary text when generating HTML.
- Only render raw HTML from trusted YAML content.
- Avoid including passwords, tokens, or other actual secrets in documentation.
- Reuse the same YAML sources across different output formats.

---

## Contributing

Contributions from the community are welcome!

There are several ways you can help improve this project:

- **Report bugs:** Found a problem? Open an issue in the [GitHub Issues tracker](https://github.com/mimdal-code/project-doc-maven-plugin/issues).
- **Suggest features:** Have an idea for a new feature or improvement? Share it through GitHub Issues.
- **Contribute code:** Pull requests fixing bugs, adding features, improving documentation, or enhancing existing functionality are welcome.
- **Fork and customize:** Feel free to fork the project and adapt it to your own requirements.

For significant changes, consider opening an issue first so the proposed approach can be discussed before implementation.

You are welcome to use, modify, fork, and redistribute this project in accordance with its license.

## License

This project is licensed under the **MIT License**.

The MIT License allows you to use, modify, distribute, and include this software in both open-source and commercial projects, subject to its terms.

See the LICENSE file for the complete license text.

---

## Links

- **GitHub Repository:** [https://github.com/mimdal-code/project-doc-maven-plugin](https://github.com/mimdal-code/project-doc-maven-plugin)
- **GitHub Issues:** [https://github.com/mimdal-code/project-doc-maven-plugin/issues](https://github.com/mimdal-code/project-doc-maven-plugin/issues)
- **GitHub Releases:** [https://github.com/mimdal-code/project-doc-maven-plugin/releases](https://github.com/mimdal-code/project-doc-maven-plugin/releases)
- **Maven Central:** [https://central.sonatype.com/artifact/com.github.mimdal/project-doc-maven-plugin](https://central.sonatype.com/artifact/com.github.mimdal/project-doc-maven-plugin)
