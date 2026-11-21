# Developer Guide

This guide explains how to create documentation with `project-doc-maven-plugin`, using HTML or Markdown templates and YAML data files.

The central idea is to keep **content**, **presentation**, and **generated output** separate:

- YAML files contain the documentation data and text.
- FreeMarker `.tmpl` files describe how that data is rendered.
- The Maven plugin combines the YAML data with a template and writes the generated document.

## 1. Decide what the generated document should look like

If you want an HTML document, start by thinking in normal HTML: headings, paragraphs, lists, links, and tables.

For example, this is the kind of HTML structure you may want to generate:

```html
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

You do not need to write a separate HTML file and fill it manually every time the documentation changes. Instead, turn this HTML structure into a **FreeMarker template** and put variable data in YAML files.

## 2. Create a `.tmpl` template

Create a template file, for example:

`template/installation-guide.html.tmpl`

The `.tmpl` extension tells you that this is a template, not the final generated HTML file. It contains ordinary HTML together with FreeMarker expressions and directives.

```html
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

The HTML tags define the document's structure. FreeMarker expressions such as `${main.document.title?html}` insert values from YAML, while directives such as `<#list>` repeat a section for each item in a YAML collection.

Use `?html` for ordinary text values so characters such as `<`, `>`, and `&` are escaped correctly. Only output raw HTML when the source is trusted and is intentionally meant to contain markup.

## 3. Create the YAML data files

The template above expects three YAML data sources. Put them in the configured source directory.

Example project structure:

```text
src/main/resources/installation-guide/
├── doc-main.yaml
├── doc-vault.yaml
├── doc-properties.yaml
└── template/
    └── installation-guide.html.tmpl
```

### `doc-main.yaml`

Use this file for general document information and the main installation-guide content.

```yaml
document:
  title: "Application Installation Guide"
  subtitle: "Installation and configuration instructions"
  version: "1.0.0"
  author: ""

environment:
  heading: "Installation Environment"
  description: >-
    Instructions for preparing and configuring the application.
```

### `doc-vault.yaml`

Use this file for documenting secret names and where they are configured. Do not put actual passwords, tokens, or secret values in documentation.

```yaml
vault:
  heading: "Vault Configuration"
  intro: >-
    The following keys must be configured in the application's secret store.
  groups:
    - heading: "Database"
      items:
        - label: "Database username"
          key: "db.username"
        - label: "Database password"
          key: "db.password"
```

### `doc-properties.yaml`

Use this file for application properties and their explanations. The template iterates over `sections` and each section's `items` to create the HTML tables.

```yaml
properties:
  heading: "Application Properties"
  description: "General application configuration."

  sections:
    - heading: "Database Configuration"
      items:
        - name: "spring.datasource.url"
          value: "jdbc:postgresql://localhost/app"
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

The names and nesting of the YAML properties form the data structure that the template accesses. For example, the `doc-properties.yaml` file is exposed through the `properties` alias, so its top-level `properties` object is accessed as `properties.properties`.

## 4. Connect the template and YAML files in Maven

Configure the plugin to load the source directory, select the template, and make the YAML files available to it through aliases.

```xml
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>

    <configuration>
        <sourceDirectory>
            ${project.basedir}/src/main/resources/installation-guide
        </sourceDirectory>

        <outputDirectory>
            ${project.build.directory}/project-doc
        </outputDirectory>

        <templates>
            <template>
                <name>installation-guide</name>
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

Each `<dataFile>` declares a YAML file and the alias by which the template can access it:

| YAML file | Template alias | Example access |
|---|---|---|
| `doc-main.yaml` | `main` | `${main.document.title}` |
| `doc-vault.yaml` | `vault` | `${vault.vault.heading}` |
| `doc-properties.yaml` | `properties` | `${properties.properties.heading}` |

The `<file>` paths are relative to `sourceDirectory`. The template's `<output>` is the name of the generated file under `outputDirectory`.

## 5. Generate the document

Run the plugin goal:

```bash
mvn project-doc:generate
```

Or run the Maven lifecycle phase to which the plugin is bound:

```bash
mvn package
```

With the example configuration, the generated file is:

```text
target/project-doc/installation-guide.html
```

The generated `.html` file is the final document. The `.html.tmpl` file remains the editable template source.

## 6. Generate Markdown instead of HTML

The same YAML data can also be rendered as Markdown. Create another template, for example `template/installation-guide.md.tmpl`:

```markdown
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

Then add another `<template>` entry with a Markdown output filename, such as `installation-guide.md`. Configure its `dataFiles` in the same way. Both templates can use the same YAML sources; only the presentation template and output filename need to differ.

## 7. Organize YAML files by documentation responsibility

You can define more than one YAML file for a template. The plugin makes each configured file available through its own alias, so a template can combine several independent sources.

As a best practice, use **one YAML file for one logical documentation responsibility**. For example:

- `doc-main.yaml` — general document information and installation instructions.
- `doc-vault.yaml` — secret-store keys and related guidance.
- `doc-properties.yaml` — application properties and their descriptions.

This is usually easier to maintain than putting every piece of documentation into one large YAML file. It also makes each source easier to understand, review, and reuse.

A single template can consume several YAML files, and several templates can reuse the same YAML files. For example, an HTML template and a Markdown template can both consume `doc-main.yaml`, `doc-vault.yaml`, and `doc-properties.yaml`.

Keep the responsibility boundaries meaningful: split files by the kind of documentation data they own, not simply to create many small files. The goal is to make the YAML structure clear and maintainable while keeping templates focused on presentation.
