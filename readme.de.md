# Project Documentation Maven Plugin

English | Français | **Deutsch** | فارسی | العربية | עברית | 简体中文

Ein Maven-Plugin zur automatischen Generierung der Projektdokumentation auf Grundlage von Vorlagen und YAML-Datendateien.

## Überblick

Das `project-doc-maven-plugin` trennt den Inhalt der Dokumentation von ihrer Darstellung. Es verwendet FreeMarker-Vorlagen und YAML-Dateien als Datenquellen, um Dokumente entsprechend den Anforderungen Ihres Projekts zu generieren.

Sie können mehrere Vorlagen definieren. Jede Vorlage kann eigene YAML-Dateien und eine eigene Ausgabedatei verwenden.

## Funktionen

- Generierung von Dokumentation mithilfe von FreeMarker-Vorlagen.
- Verwendung von YAML-Dateien als Datenquellen.
- Unterstützung mehrerer Vorlagen und Ausgabeformate.
- Zuordnung mehrerer YAML-Dateien zu einer Vorlage.
- Verwendung von `template-key` zur Festlegung der Variablennamen, die in Vorlagen verfügbar sind.
- Generierung von Dateien in einem konfigurierbaren Ausgabeverzeichnis.
- `scaffold`-Befehl zum Erstellen einer initialen Dokumentationsstruktur.
- Wiederverwendung derselben YAML-Dateien in mehreren Vorlagen.

## Voraussetzungen

- Java 17 oder höher.
- Apache Maven 3.6.3 oder höher.

## Installation

Fügen Sie das Plugin im Abschnitt `<build><plugins>` Ihrer `pom.xml` hinzu:

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
</plugin>
```

Ersetzen Sie `${plugin.version}` durch die gewünschte Plugin-Version.

## Verwendung

Das Plugin stellt zwei Maven-Ziele bereit:

| Ziel                          | Beschreibung                                                             |
| ----------------------------- | ------------------------------------------------------------------------ |
| `installation-guide:scaffold` | Erstellt die initialen Dokumentationsdateien des Projekts.               |
| `installation-guide:generate` | Generiert Dokumente anhand der konfigurierten Vorlagen und YAML-Dateien. |

### 1. Initiale Struktur erstellen

Führen Sie folgenden Befehl aus:

```
mvn installation-guide:scaffold
```

Dieser Befehl erstellt die initialen Dateien im Verzeichnis `project-doc/` Ihres Projekts.

Verhalten:

- Es werden nur Dateien erstellt, die noch nicht vorhanden sind.
- Vorhandene Dateien werden niemals überschrieben.
- Der Befehl kann gefahrlos mehrfach ausgeführt werden.
- Erstellt und übersprungene Dateien werden angezeigt.
- Die Datei `pom.xml` wird nicht verändert.
- Es werden keine Dokumente generiert.

### 2. Generierung konfigurieren

Fügen Sie Ihrem Plugin in der `pom.xml` folgende Konfiguration hinzu:

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

Jedes Element `<template>` definiert ein zu generierendes Dokument:

| Element     | Beschreibung                                                     |
| ----------- | ---------------------------------------------------------------- |
| `name`      | Name zur Identifizierung der Vorlagenkonfiguration.              |
| `file`      | Pfad zur FreeMarker-Vorlagendatei.                               |
| `output`    | Name der generierten Datei im Ausgabeverzeichnis.                |
| `dataFiles` | Liste der YAML-Dateien, die von dieser Vorlage verwendet werden. |

Relative Pfade werden ausgehend vom Maven-Projektverzeichnis (`${project.basedir}`) aufgelöst. Sie können auch absolute Pfade verwenden. Es wird empfohlen, `${project.basedir}` in der Konfiguration zu verwenden, damit die Pfade eindeutig sind.

Der Ausgabepfad ist relativ zu `outputDirectory`.

### 3. YAML-Dateien vorbereiten

Jede YAML-Datei muss eine obligatorische Eigenschaft `template-key` auf der obersten Ebene enthalten.

Diese Eigenschaft bestimmt den Namen der Variablen, die in der FreeMarker-Vorlage verfügbar ist.

Beispiel: `doc-main.yaml`

```
template-key: main

document:
  title: "Installationsanleitung"
  description: "Installationsanleitung für das Projekt"
```

Beispiel: `doc-vault.yaml`

```
template-key: vault

vault:
  heading: "Tresorkonfiguration"
  enabled: true
```

Beispiel: `doc-properties.yaml`

```
template-key: properties

properties:
  heading: "Eigenschaftskonfiguration"
  values:
    server.port: 8080
    logging.level: INFO
```

In diesem Beispiel stellen die drei YAML-Dateien folgende Variablen bereit:

| YAML-Datei            | `template-key` | FreeMarker-Variable |
| --------------------- | -------------- | ------------------- |
| `doc-main.yaml`       | `main`         | `main`              |
| `doc-vault.yaml`      | `vault`        | `vault`             |
| `doc-properties.yaml` | `properties`   | `properties`        |

**Wichtig:**

- `template-key` muss in jeder YAML-Datei auf der obersten Ebene vorhanden sein.
- Der Wert muss eine nicht leere Zeichenfolge sein.
- Die Werte von `template-key` müssen innerhalb der YAML-Dateien einer Vorlage eindeutig sein.
- Der Dateiname der YAML-Datei hat keinen Einfluss auf den Namen der FreeMarker-Variablen.
- `template-key` dient zur Identifizierung der Daten und wird nicht in das an die Vorlage übergebene Datenobjekt aufgenommen.
- Für jede YAML-Datei muss kein Maven-Element `<name>` als Alias definiert werden.

### 4. Daten in einer FreeMarker-Vorlage verwenden

Die Daten jeder YAML-Datei sind über die Variable verfügbar, die durch `template-key` festgelegt wurde.

Beispiel:

```
# ${main.document.title}

${main.document.description}

## ${vault.vault.heading}

Status: ${vault.vault.enabled?string("Aktiviert", "Deaktiviert")}

## ${properties.properties.heading}

Server-Port: ${properties.properties.values["server.port"]}
```

Die Variablen `main`, `vault` und `properties` entsprechen den `template-key`-Werten der jeweiligen YAML-Dateien.

Sie können alle von FreeMarker unterstützten Funktionen verwenden, um die Daten zu formatieren und darzustellen.

### 5. Dokumentation generieren

Führen Sie folgenden Befehl aus:

```
mvn installation-guide:generate
```

Die Dokumente werden im über `outputDirectory` konfigurierten Verzeichnis erstellt.

## Mehrere Vorlagen

Sie können mehrere `<template>`-Elemente definieren, um verschiedene Dokumente oder Ausgabeformate aus denselben Daten zu generieren.

Beispiel:

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

Jede Vorlage gibt explizit die YAML-Dateien an, die sie benötigt. Dieselben YAML-Dateien können in mehreren Vorlagen wiederverwendet werden.

## Integration in den Maven-Lebenszyklus

Das Ziel `generate` ist standardmäßig keiner Phase des Maven-Lebenszyklus zugeordnet.

Sie können es einer Phase zuordnen, indem Sie eine Ausführung in Ihrer `pom.xml` konfigurieren:

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

Passen Sie die Maven-Phase an Ihre Anforderungen an.

Das Ziel `scaffold` ist für die manuelle Ausführung vorgesehen und sollte nicht automatisch an den Lebenszyklus gebunden werden.

## Konfigurationsreferenz

| Parameter                           | Beschreibung                                                      |
| ----------------------------------- | ----------------------------------------------------------------- |
| `outputDirectory`                   | Verzeichnis, in dem die generierten Dokumente gespeichert werden. |
| `templates`                         | Liste der Vorlagenkonfigurationen.                                |
| `templates.template.name`           | Name zur Identifizierung einer Vorlagenkonfiguration.             |
| `templates.template.file`           | Pfad zur FreeMarker-Vorlagendatei.                                |
| `templates.template.output`         | Name der generierten Datei.                                       |
| `templates.template.dataFiles`      | YAML-Dateien, die von der Vorlage verwendet werden.               |
| `templates.template.dataFiles.file` | Pfad zu einer YAML-Datei.                                         |

## Empfehlungen

- Trennen Sie den Dokumentationsinhalt von seiner Darstellung.
- Verwenden Sie separate YAML-Dateien, um verschiedene Datenkategorien zu organisieren.
- Wählen Sie aussagekräftige und eindeutige Werte für `template-key`.
- Geben Sie für jede Vorlage nur die benötigten YAML-Dateien an.
- Verwenden Sie `${project.basedir}`, um die Pfade zu Projektressourcen eindeutig festzulegen.
- Bewahren Sie Vorlagen in einem eigenen Verzeichnis auf.
- Verwenden Sie mehrere Vorlagen, wenn Sie aus denselben Daten verschiedene Ausgabeformate erzeugen möchten.

## Mitwirken

Beiträge sind willkommen.

Sie können mitwirken, indem Sie Fehler melden, Verbesserungen vorschlagen oder Pull Requests einreichen.

## Lizenz

Die Lizenzbedingungen finden Sie in der Datei `LICENSE` des Projekts.

## Links

- [GitHub-Repository](https://github.com/mimdal-code/project-doc-maven-plugin)
- [Fehler melden](https://github.com/mimdal-code/project-doc-maven-plugin/issues)
