# Project Documentation Maven Plugin

English | **Français** | Deutsch | فارسی | العربية | עברית | 简体中文

Un plugin Maven permettant de générer automatiquement la documentation d'un projet à partir de modèles et de fichiers de données YAML.

## Présentation

Le plugin `project-doc-maven-plugin` sépare le contenu de la documentation de sa présentation. Il utilise des modèles FreeMarker et des fichiers YAML pour générer des documents adaptés aux besoins de votre projet.

Vous pouvez définir plusieurs modèles, chacun avec ses propres fichiers YAML et son propre fichier de sortie.

## Fonctionnalités

- Génération de documentation à partir de modèles FreeMarker.
- Utilisation de fichiers YAML comme sources de données.
- Plusieurs modèles et formats de sortie.
- Association de plusieurs fichiers YAML à un même modèle.
- Utilisation de `template-key` pour définir les noms des variables accessibles dans les modèles.
- Génération de fichiers dans un répertoire de sortie configurable.
- Commande `scaffold` pour créer une structure de documentation initiale.
- Possibilité de réutiliser les mêmes fichiers YAML dans plusieurs modèles.

## Prérequis

- Java 17 ou version ultérieure.
- Apache Maven 3.6.3 ou version ultérieure.

## Installation

Ajoutez le plugin à la section `<build><plugins>` de votre fichier `pom.xml` :

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
</plugin>
```

Remplacez `${plugin.version}` par la version souhaitée du plugin.

## Utilisation

Le plugin fournit deux objectifs Maven :

| Objectif                      | Description                                                                |
| ----------------------------- | -------------------------------------------------------------------------- |
| `installation-guide:scaffold` | Crée les fichiers de démarrage de la documentation du projet.              |
| `installation-guide:generate` | Génère les documents à partir des modèles et des fichiers YAML configurés. |

### 1. Créer la structure initiale

Exécutez :

```
mvn installation-guide:scaffold
```

Cette commande crée les fichiers de démarrage dans le répertoire `project-doc/` du projet.

Comportement :

- Crée uniquement les fichiers qui n'existent pas.
- Ne remplace jamais les fichiers existants.
- Peut être exécutée plusieurs fois sans risque.
- Indique les fichiers créés et ceux ignorés.
- Ne modifie pas le fichier `pom.xml`.
- Ne génère pas les documents.

### 2. Configurer la génération

Ajoutez la configuration suivante à votre plugin dans `pom.xml` :

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

Chaque élément `<template>` définit un document à générer :

| Élément     | Description                                         |
| ----------- | --------------------------------------------------- |
| `name`      | Nom identifiant la configuration du modèle.         |
| `file`      | Chemin du fichier de modèle FreeMarker.             |
| `output`    | Nom du fichier généré dans le répertoire de sortie. |
| `dataFiles` | Liste des fichiers YAML utilisés par ce modèle.     |

Les chemins relatifs sont résolus à partir du répertoire du projet Maven (`${project.basedir}`). Vous pouvez utiliser des chemins absolus. Il est recommandé d'utiliser `${project.basedir}` dans votre configuration pour rendre les chemins explicites.

Le chemin de sortie est relatif à `outputDirectory`.

### 3. Préparer les fichiers YAML

Chaque fichier YAML doit définir une propriété obligatoire `template-key` à sa racine.

Cette propriété détermine le nom de la variable qui sera disponible dans le modèle FreeMarker.

Exemple : `doc-main.yaml`

```
template-key: main

document:
  title: "Guide d'installation"
  description: "Guide d'installation du projet"
```

Exemple : `doc-vault.yaml`

```
template-key: vault

vault:
  heading: "Configuration du coffre-fort"
  enabled: true
```

Exemple : `doc-properties.yaml`

```
template-key: properties

properties:
  heading: "Configuration des propriétés"
  values:
    server.port: 8080
    logging.level: INFO
```

Dans cet exemple, les trois fichiers YAML exposent les variables suivantes :

| Fichier YAML          | `template-key` | Variable FreeMarker |
| --------------------- | -------------- | ------------------- |
| `doc-main.yaml`       | `main`         | `main`              |
| `doc-vault.yaml`      | `vault`        | `vault`             |
| `doc-properties.yaml` | `properties`   | `properties`        |

**Important :**

- `template-key` doit être présent à la racine de chaque fichier YAML.
- Sa valeur doit être une chaîne non vide.
- Les valeurs de `template-key` doivent être uniques parmi les fichiers YAML associés à un même modèle.
- Le nom du fichier YAML n'a aucune influence sur le nom de la variable FreeMarker.
- La propriété `template-key` est utilisée pour identifier les données et n'est pas incluse dans l'objet de données transmis au modèle.
- Il n'est pas nécessaire de déclarer un alias avec un élément Maven `<name>` pour chaque fichier YAML.

### 4. Utiliser les données dans un modèle FreeMarker

Les données de chaque fichier YAML sont accessibles à partir de la variable définie par son `template-key`.

Exemple :

```
# ${main.document.title}

${main.document.description}

## ${vault.vault.heading}

État : ${vault.vault.enabled?string("Activé", "Désactivé")}

## ${properties.properties.heading}

Port du serveur : ${properties.properties.values["server.port"]}
```

Les variables `main`, `vault` et `properties` correspondent aux valeurs `template-key` des fichiers YAML.

Vous pouvez utiliser toutes les fonctionnalités prises en charge par FreeMarker pour formater et afficher les données.

### 5. Générer la documentation

Exécutez :

```
mvn installation-guide:generate
```

Les documents sont générés dans le répertoire configuré par `outputDirectory`.

## Plusieurs modèles

Vous pouvez définir plusieurs éléments `<template>` pour générer différents documents ou formats à partir des mêmes données.

Exemple :

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

Chaque modèle déclare explicitement les fichiers YAML dont il a besoin. Les mêmes fichiers YAML peuvent être réutilisés dans plusieurs modèles.

## Intégration au cycle de vie Maven

L'objectif `generate` n'est associé à aucune phase du cycle de vie Maven par défaut.

Vous pouvez l'associer à une phase en configurant une exécution dans votre `pom.xml` :

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

Adaptez la phase Maven à vos besoins.

L'objectif `scaffold` est destiné à être exécuté manuellement et ne doit pas être associé automatiquement au cycle de vie.

## Configuration de référence

| Paramètre                           | Description                                                    |
| ----------------------------------- | -------------------------------------------------------------- |
| `outputDirectory`                   | Répertoire dans lequel les documents générés sont enregistrés. |
| `templates`                         | Liste des configurations de modèles.                           |
| `templates.template.name`           | Nom identifiant une configuration de modèle.                   |
| `templates.template.file`           | Chemin du fichier de modèle FreeMarker.                        |
| `templates.template.output`         | Nom du fichier généré.                                         |
| `templates.template.dataFiles`      | Fichiers YAML utilisés par le modèle.                          |
| `templates.template.dataFiles.file` | Chemin d'un fichier YAML.                                      |

## Bonnes pratiques

- Séparez le contenu des documents de leur présentation.
- Utilisez des fichiers YAML distincts pour organiser les différentes catégories de données.
- Choisissez des valeurs `template-key` explicites et uniques.
- Déclarez uniquement les fichiers YAML nécessaires à chaque modèle.
- Utilisez `${project.basedir}` pour définir clairement les chemins des ressources du projet.
- Conservez les modèles dans un répertoire dédié.
- Utilisez plusieurs modèles lorsque vous souhaitez produire différents formats à partir des mêmes données.

## Contribution

Les contributions sont les bienvenues.

Vous pouvez contribuer en signalant des problèmes, en proposant des améliorations ou en soumettant des demandes de fusion.

## Licence

Consultez le fichier `LICENSE` du projet pour connaître les conditions de licence.

## Liens

- [Dépôt GitHub](https://github.com/mimdal-code/project-doc-maven-plugin)
- [Signaler un problème](https://github.com/mimdal-code/project-doc-maven-plugin/issues)

