# project-doc-maven-plugin



**Languages:** English | Français | Deutsch | فارسی | العربية | עברית | 简体中文

# About

A configurable Maven plugin for generating project documentation during the build lifecycle.

Project documentation has two distinct responsibilities:

- **Presentation:** how the document is structured and rendered, including its layout, formatting, and visual appearance.
- **Content:** what the document says, including descriptions, installation instructions, configuration properties, and other project-specific information.

During application development, documentation content changes frequently. New features are introduced, configuration properties change, and installation instructions evolve. These updates should not require developers to modify the document's presentation. Mixing content with rendering logic makes documentation harder to maintain and increases the risk of accidentally breaking templates while making a content-only change.

This plugin separates these responsibilities: YAML files hold documentation content, while FreeMarker templates control its presentation. The Maven plugin combines them to generate the final document. Developers can therefore focus on updating project information without having to change rendering logic every time the application evolves.

The same content can also be reused by multiple templates, allowing different output formats or document layouts to be generated from the same YAML sources.

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

### Generate starter files

Run the scaffold goal to create editable sample files:

```
mvn installation-guide:scaffold
```

It creates `project-doc/template/installation-guide.html.tmpl` and the three YAML files under `project-doc/data/`. Existing files are never overwritten, so the goal is safe to run repeatedly.

The scaffold goal does not modify `pom.xml` and does not configure or execute documentation generation.

### Generate documentation

You can invoke the generation goal directly:

```
mvn installation-guide:generate
```

You can also bind the goal to any Maven lifecycle phase by declaring an execution. The plugin does not select a default phase; the consuming project controls when generation runs. For example, to generate documentation during `prepare-package`:

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
    <executions>
        <execution>
            <id>generate-project-documentation</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Replace `prepare-package` with any lifecycle phase appropriate for your build, such as `generate-resources`, `process-resources`, `package`, or `verify`. Keep the plugin's `<configuration>` in the same `<plugin>` element. The `scaffold` goal remains a separate, manually invoked command and is not bound to the lifecycle.

## Configuration

The plugin accepts explicit template and YAML file paths. Relative paths are resolved from `${project.basedir}`; absolute paths are used as supplied. There is no shared source directory or required project layout.

For clarity, the examples use `${project.basedir}` for template and YAML paths. This is a Maven predefined property containing the absolute path to the consuming project's root directory.

### Example configuration

The following example uses the HTML template generated by the scaffold goal:

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>

    <configuration>
        <outputDirectory>
            ${project.build.directory}/project-doc
        </outputDirectory>

        <templates>
            <template>
                <name>installation-guide</name>
                <file>${project.basedir}/project-doc/template/installation-guide.html.tmpl</file>
                <output>installation-guide.html</output>

                <dataFiles>
                    <file>${project.basedir}/project-doc/data/doc-main.yaml</file>
                    <file>${project.basedir}/project-doc/data/doc-vault.yaml</file>
                    <file>${project.basedir}/project-doc/data/doc-properties.yaml</file>
                </dataFiles>
            </template>
        </templates>
    </configuration>
</plugin>
```

Each `<file>` entry identifies one YAML data file. The corresponding YAML file defines its own `template-key`, which determines the variable name available inside the FreeMarker template.

### Configuration reference

| Configuration             | Description                                          |
| ------------------------- | ---------------------------------------------------- |
| `outputDirectory`         | Directory where generated documents are written.     |
| `templates`               | Collection of template configurations.               |
| `template.name`           | Logical name identifying a template configuration.   |
| `template.file`           | FreeMarker template file path; relative or absolute. |
| `template.output`         | Relative output path under `outputDirectory`.        |
| `template.dataFiles`      | YAML data files made available to the template.      |
| `template.dataFiles.file` | Path to a YAML data file; relative or absolute.      |

### File path resolution

Relative template and YAML paths are resolved from `${project.basedir}`. Absolute paths are used directly. Files may be stored anywhere accessible to the Maven process; they do not need to share a directory or follow a prescribed structure.

Using `${project.basedir}` explicitly is recommended because it makes the resolved path independent of the directory from which Maven is invoked.

Each template's `<output>` is a relative path under `<outputDirectory>`.

### Template configuration

Each template configuration defines one output document.

| Property    | Description                                           |
| ----------- | ----------------------------------------------------- |
| `name`      | Logical template identifier.                          |
| `file`      | FreeMarker template file.                             |
| `output`    | Generated output path, relative to `outputDirectory`. |
| `dataFiles` | YAML data sources available to the template.          |

You can define multiple templates to generate different documents or formats from the same YAML sources.

### YAML data file configuration

Each template can declare multiple YAML files using repeated `<file>` elements:

```
<dataFiles>
    <file>${project.basedir}/project-doc/data/doc-main.yaml</file>
    <file>${project.basedir}/project-doc/data/doc-vault.yaml</file>
</dataFiles>
```

There is no separate alias or name element in the Maven configuration. Each YAML file must declare a root-level `template-key` property. The value of this property determines the variable name used to access that file's data inside the FreeMarker template.

For example, a YAML file containing:

```
template-key: main

document:
  title: "Installation Guide"
```

makes its data available through `main`:

```
${main.document.title}
```

The filename does not determine the variable name. You can rename or move a YAML file without changing its `template-key`.

Each `template-key` must be unique among the YAML files configured for the same template.

## Project structure

The scaffold goal creates a suggested structure, but the generate goal does not require it:

```
project-doc/
├── template/
│   └── installation-guide.html.tmpl
└── data/
    ├── doc-main.yaml
    ├── doc-vault.yaml
    └── doc-properties.yaml

target/
└── project-doc/
    └── installation-guide.html
```

You can move or rename the template and YAML files and update their paths in the Maven configuration. The plugin uses the exact paths you configure.

---

## Working with YAML

YAML files define the documentation content independently of the output format.

This separation allows you to maintain the content in one place and reuse it across multiple templates.

Each YAML file must contain a root-level `template-key` property. This property identifies the data variable exposed to FreeMarker. The remaining YAML content is available under that variable.

The following examples demonstrate one possible documentation structure.

### 1. Main document — `doc-main.yaml`

Contains document metadata and installation environment information.

```
template-key: main

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
template-key: vault

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
template-key: properties

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

### Start with the document structure

When creating an HTML document, first decide how it should look using ordinary HTML: headings, paragraphs, lists, links, and tables. For example:

```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Installation Guide</title>
</head>
<body>
    <h1>Installation Guide</h1>
    <p>This document describes the application configuration.</p>

    <h2>Application Properties</h2>
    <table>
        <thead>
            <tr>
                <th>Property</th>
                <th>Example value</th>
                <th>Description</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>spring.datasource.url</td>
                <td>jdbc:postgresql://localhost/app</td>
                <td>Database connection URL</td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```

Instead of manually editing this HTML whenever the application documentation changes, turn the structure into a FreeMarker template and replace changing values with expressions that read from YAML data files.

### Example HTML template

For example, `installation-guide.html.tmpl` can contain:

```
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${main.document.title?html}</title>
</head>
<body>
    <h1>${main.document.title?html}</h1>

    <#if main.document.subtitle??>
        <p>${main.document.subtitle?html}</p>
    </#if>

    <h2>${properties.properties.heading?html}</h2>

    <#list properties.properties.sections as section>
        <h3>${section.heading?html}</h3>

        <table>
            <thead>
                <tr>
                    <th>Property</th>
                    <th>Example value</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
                <#list section.items as item>
                    <tr>
                        <td>${item.name?html}</td>
                        <td>${item.value?html}</td>
                        <td>${item.description?html}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </#list>
</body>
</html>
```

The HTML elements define the presentation. FreeMarker expressions insert values from the YAML data variables, and directives such as `<#if>` and `<#list>` handle optional content and collections. The following sections explain how to access YAML values, iterate over collections, and handle optional properties.

### Accessing YAML data

Each YAML file is exposed through the value of its `template-key` property.

For example, consider `doc-main.yaml`:

```
template-key: main

document:
  title: "Installation Guide"
  version: "1.0.0"
```

The YAML data becomes available through the `main` variable.

You can access its properties using:

```
${main.document.title}
${main.document.version}
```

Similarly, the other YAML files in the examples are available as:

```
${vault.vault.heading}
${vault.vault.intro}

${properties.properties.heading}
${properties.properties.description}
```

The first part is the value of `template-key`. The remaining parts follow the YAML structure.

### Accessing nested properties

YAML objects are loaded as nested data structures.

For example:

```
template-key: main

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
template-key: vault

vault:
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
template-key: vault

vault:
  footnote: >-
    Configure the secret using
    <span class="key">keycloak.client-secret</span>.
```

If the HTML template should preserve that markup, render it directly:

```
<div class="note">
    <p>${vault.vault.footnote}</p>
</div>
```

Only use this approach with trusted HTML content. For ordinary text, use `?html`.

### Multiple output formats

The same YAML content can be rendered into Markdown as well. For example, a Markdown template can use:

```
# ${main.document.title}

${main.document.subtitle}

## ${properties.properties.heading}

<#list properties.properties.sections as section>
### ${section.heading}

| Property | Example value | Description |
|---|---|---|
<#list section.items as item>
| ${item.name} | ${item.value} | ${item.description} |
</#list>

</#list>
```

You can define multiple templates that use the same YAML sources.

For example:

| Template                       | Output                    |
| ------------------------------ | ------------------------- |
| `index.md.tmpl`                | `index.md`                |
| `installation-guide.html.tmpl` | `installation-guide.html` |

Both templates can access the same data variables:

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
- Give each YAML file a meaningful and unique `template-key` within its template configuration.
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