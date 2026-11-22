# Project Documentation Maven Plugin

English | Français | Deutsch | فارسی | العربية | עברית | **简体中文**

一个 Maven 插件，用于通过模板和 YAML 数据文件自动生成项目文档。

## 概述

`project-doc-maven-plugin` 将文档内容与展示形式分离。它使用 FreeMarker 模板和 YAML 文件作为数据源，根据项目需求生成文档。

您可以定义多个模板，每个模板都可以使用自己的 YAML 文件和输出文件。

## 功能特性

- 使用 FreeMarker 模板生成文档。
- 使用 YAML 文件作为数据源。
- 支持多个模板和输出格式。
- 一个模板可以关联多个 YAML 文件。
- 使用 `template-key` 定义模板中可访问的数据变量名称。
- 在可配置的输出目录中生成文件。
- 提供 `scaffold` 命令，用于创建初始文档结构。
- 多个模板可以重复使用相同的 YAML 文件。

## 环境要求

- Java 17 或更高版本。
- Apache Maven 3.6.3 或更高版本。

## 安装

将插件添加到 `pom.xml` 文件的 `<build><plugins>` 部分：

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
</plugin>
```

将 `${plugin.version}` 替换为所需的插件版本。

## 使用方法

该插件提供两个 Maven 目标：

| 目标                            | 说明                     |
| ----------------------------- | ---------------------- |
| `installation-guide:scaffold` | 创建项目文档的初始文件。           |
| `installation-guide:generate` | 根据已配置的模板和 YAML 文件生成文档。 |

### 1. 创建初始结构

执行以下命令：

```
mvn installation-guide:scaffold
```

该命令会在项目的 `project-doc/` 目录下创建初始文件。

行为说明：

- 仅创建尚不存在的文件。
- 绝不会覆盖已有文件。
- 可以安全地重复执行。
- 显示已创建和已跳过的文件。
- 不会修改 `pom.xml` 文件。
- 不会生成文档。

### 2. 配置文档生成

在 `pom.xml` 中为插件添加以下配置：

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

每个 `<template>` 元素定义一个需要生成的文档：

| 元素          | 说明                  |
| ----------- | ------------------- |
| `name`      | 用于标识模板配置的名称。        |
| `file`      | FreeMarker 模板文件的路径。 |
| `output`    | 在输出目录中生成的文件名。       |
| `dataFiles` | 此模板使用的 YAML 文件列表。   |

相对路径以 Maven 项目目录（`${project.basedir}`）为基准解析。也可以使用绝对路径。建议在配置中使用 `${project.basedir}`，以明确指定资源路径。

输出文件的路径相对于 `outputDirectory`。

### 3. 准备 YAML 文件

每个 YAML 文件都必须在根级别定义一个名为 `template-key` 的必需属性。

该属性决定了数据在 FreeMarker 模板中对应的变量名称。

示例：`doc-main.yaml`

```
template-key: main

document:
  title: "安装指南"
  description: "项目安装指南"
```

示例：`doc-vault.yaml`

```
template-key: vault

vault:
  heading: "密钥库配置"
  enabled: true
```

示例：`doc-properties.yaml`

```
template-key: properties

properties:
  heading: "属性配置"
  values:
    server.port: 8080
    logging.level: INFO
```

在此示例中，三个 YAML 文件分别提供以下变量：

| YAML 文件               | `template-key` | FreeMarker 变量 |
| --------------------- | -------------- | ------------- |
| `doc-main.yaml`       | `main`         | `main`        |
| `doc-vault.yaml`      | `vault`        | `vault`       |
| `doc-properties.yaml` | `properties`   | `properties`  |

**重要说明：**

- 每个 YAML 文件都必须在根级别包含 `template-key`。
- 其值必须是非空字符串。
- 同一个模板关联的 YAML 文件之间，`template-key` 的值必须唯一。
- YAML 文件名不会影响 FreeMarker 变量的名称。
- `template-key` 用于标识数据，不会包含在传递给模板的数据对象中。
- 不需要通过 Maven 的 `<name>` 元素为每个 YAML 文件定义别名。

### 4. 在 FreeMarker 模板中使用数据

可以通过每个 YAML 文件的 `template-key` 所指定的变量访问该文件中的数据。

示例：

```
# ${main.document.title}

${main.document.description}

## ${vault.vault.heading}

状态：${vault.vault.enabled?string("已启用", "已禁用")}

## ${properties.properties.heading}

服务器端口：${properties.properties.values["server.port"]}
```

变量 `main`、`vault` 和 `properties` 分别对应 YAML 文件中的 `template-key` 值。

您可以使用 FreeMarker 支持的所有功能来格式化和展示数据。

### 5. 生成文档

执行以下命令：

```
mvn installation-guide:generate
```

生成的文档将保存在通过 `outputDirectory` 配置的目录中。

## 使用多个模板

您可以定义多个 `<template>` 元素，使用相同的数据生成不同的文档或输出格式。

示例：

```
<templates>
    <template>
        <name>markdown</name>
        <file>${project.basedir}/project-doc/template/index.md.tmpl</file>
        <output>index.md</output>
        <dataFiles>
            <file>${project.basedir}/project-doc/data/doc-main.yaml</file>
            <file>${project.basedir}/project-doc/data/doc-vault.yaml</file>
        </dataFiles>
    </template>

    <template>
        <name>html</name>
        <file>${project.basedir}/project-doc/template/index.html.tmpl</file>
        <output>index.html</output>
        <dataFiles>
            <file>${project.basedir}/project-doc/data/doc-main.yaml</file>
            <file>${project.basedir}/project-doc/data/doc-vault.yaml</file>
            <file>${project.basedir}/project-doc/data/doc-properties.yaml</file>
        </dataFiles>
    </template>
</templates>
```

每个模板都会明确声明自己需要的 YAML 文件。同一个 YAML 文件可以在多个模板中重复使用。

## 集成到 Maven 生命周期

默认情况下，`generate` 目标不会绑定到 Maven 生命周期中的任何阶段。

您可以在 `pom.xml` 中配置执行，将其绑定到指定阶段：

```
<executions>
    <execution>
        <id>generate-project-doc</id>
        <phase>prepare-package</phase>
        <goals>
            <goal>generate</goal>
        </goals>
    </execution>
</executions>
```

请根据项目需求选择合适的 Maven 阶段。

`scaffold` 目标用于手动执行，不应自动绑定到 Maven 生命周期。

## 配置参考

| 参数                                  | 说明                  |
| ----------------------------------- | ------------------- |
| `outputDirectory`                   | 保存生成文档的目录。          |
| `templates`                         | 模板配置列表。             |
| `templates.template.name`           | 用于标识模板配置的名称。        |
| `templates.template.file`           | FreeMarker 模板文件的路径。 |
| `templates.template.output`         | 生成文件的名称。            |
| `templates.template.dataFiles`      | 模板使用的 YAML 文件。      |
| `templates.template.dataFiles.file` | YAML 文件的路径。         |

## 最佳实践

- 将文档内容与展示形式分离。
- 使用独立的 YAML 文件组织不同类别的数据。
- 为 `template-key` 选择清晰且唯一的值。
- 每个模板只声明自己需要的 YAML 文件。
- 使用 `${project.basedir}` 明确指定项目资源的路径。
- 将模板保存在专用目录中。
- 如果需要从相同的数据生成不同格式的文档，请使用多个模板。

## 参与贡献

欢迎参与项目贡献。

您可以通过报告问题、提出改进建议或提交 Pull Request 来帮助改进项目。

## 许可证

有关许可证条款，请参阅项目中的 `LICENSE` 文件。

## 链接

- [GitHub 仓库](https://github.com/mimdal-code/project-doc-maven-plugin)
- [报告问题](https://github.com/mimdal-code/project-doc-maven-plugin/issues)