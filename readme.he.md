# Project Documentation Maven Plugin

English | Français | Deutsch | فارسی | العربية | **עברית** | 简体中文

תוסף Maven ליצירה אוטומטית של תיעוד פרויקטים באמצעות תבניות וקובצי נתונים בפורמט YAML.

## סקירה כללית

התוסף `project-doc-maven-plugin` מפריד בין תוכן התיעוד לבין אופן הצגתו. הוא משתמש בתבניות FreeMarker ובקובצי YAML כמקורות נתונים, כדי ליצור מסמכים בהתאם לצורכי הפרויקט שלך.

ניתן להגדיר מספר תבניות. לכל תבנית יכולים להיות קובצי YAML וקובץ פלט משלה.

## תכונות

- יצירת תיעוד באמצעות תבניות FreeMarker.
- שימוש בקובצי YAML כמקורות נתונים.
- תמיכה במספר תבניות ובפורמטי פלט שונים.
- שיוך מספר קובצי YAML לתבנית אחת.
- שימוש ב-`template-key` כדי לקבוע את שמות המשתנים הזמינים בתבניות.
- יצירת קבצים בתיקיית פלט שניתנת להגדרה.
- פקודת `scaffold` ליצירת מבנה ראשוני לתיעוד.
- שימוש חוזר באותם קובצי YAML במספר תבניות.

## דרישות מקדימות

- Java 17 ומעלה.
- Apache Maven 3.6.3 ומעלה.

## התקנה

הוסף את התוסף למקטע `<build><plugins>` בקובץ `pom.xml`:

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
</plugin>
```

החלף את `${plugin.version}` בגרסת התוסף הרצויה.

## שימוש

התוסף מספק שתי מטרות Maven:

| מטרה                          | תיאור                                             |
| ----------------------------- | ------------------------------------------------- |
| `installation-guide:scaffold` | יוצרת את קובצי התיעוד הראשוניים של הפרויקט.       |
| `installation-guide:generate` | יוצרת מסמכים באמצעות התבניות וקובצי YAML שהוגדרו. |

### 1. יצירת המבנה הראשוני

הרץ את הפקודה:

```
mvn installation-guide:scaffold
```

פקודה זו יוצרת את הקבצים הראשוניים בתיקייה `project-doc/` של הפרויקט שלך.

אופן הפעולה:

- נוצרים רק קבצים שעדיין אינם קיימים.
- קבצים קיימים לעולם אינם נדרסים.
- ניתן להריץ את הפקודה שוב ושוב בבטחה.
- הפקודה מדווחת אילו קבצים נוצרו ואילו קבצים דולגו.
- הקובץ `pom.xml` אינו משתנה.
- לא נוצרים מסמכים.

### 2. הגדרת יצירת התיעוד

הוסף את ההגדרה הבאה לתוסף בקובץ `pom.xml`:

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

כל רכיב `<template>` מגדיר מסמך ליצירה:

| רכיב        | תיאור                                |
| ----------- | ------------------------------------ |
| `name`      | שם לזיהוי הגדרת התבנית.              |
| `file`      | הנתיב לקובץ תבנית FreeMarker.        |
| `output`    | שם הקובץ שייווצר בתיקיית הפלט.       |
| `dataFiles` | רשימת קובצי YAML שבהם התבנית משתמשת. |

נתיבים יחסיים נפתרים ביחס לתיקיית פרויקט Maven (`${project.basedir}`). ניתן להשתמש גם בנתיבים מוחלטים. מומלץ להשתמש ב-`${project.basedir}` בהגדרות כדי להבהיר את הנתיבים.

נתיב הפלט הוא יחסי ל-`outputDirectory`.

### 3. הכנת קובצי YAML

כל קובץ YAML חייב להכיל מאפיין חובה בשם `template-key` ברמת השורש.

מאפיין זה קובע את שם המשתנה שיהיה זמין בתבנית FreeMarker.

דוגמה: `doc-main.yaml`

```
template-key: main

document:
  title: "מדריך התקנה"
  description: "מדריך ההתקנה של הפרויקט"
```

דוגמה: `doc-vault.yaml`

```
template-key: vault

vault:
  heading: "הגדרות הכספת"
  enabled: true
```

דוגמה: `doc-properties.yaml`

```
template-key: properties

properties:
  heading: "הגדרות מאפיינים"
  values:
    server.port: 8080
    logging.level: INFO
```

בדוגמה זו, שלושת קובצי YAML חושפים את המשתנים הבאים:

| קובץ YAML             | `template-key` | משתנה FreeMarker |
| --------------------- | -------------- | ---------------- |
| `doc-main.yaml`       | `main`         | `main`           |
| `doc-vault.yaml`      | `vault`        | `vault`          |
| `doc-properties.yaml` | `properties`   | `properties`     |

**הערות חשובות:**

- `template-key` חייב להופיע ברמת השורש בכל קובץ YAML.
- הערך שלו חייב להיות מחרוזת שאינה ריקה.
- ערכי `template-key` חייבים להיות ייחודיים בין קובצי ה-YAML המשויכים לאותה תבנית.
- שם קובץ ה-YAML אינו משפיע על שם משתנה FreeMarker.
- המאפיין `template-key` משמש לזיהוי הנתונים ואינו נכלל באובייקט הנתונים שמועבר לתבנית.
- אין צורך להגדיר כינוי לכל קובץ YAML באמצעות רכיב Maven בשם `<name>`.

### 4. שימוש בנתונים בתבנית FreeMarker

ניתן לגשת לנתונים של כל קובץ YAML באמצעות המשתנה ששמו נקבע על ידי `template-key` שלו.

דוגמה:

```
# ${main.document.title}

${main.document.description}

## ${vault.vault.heading}

מצב: ${vault.vault.enabled?string("מופעל", "מושבת")}

## ${properties.properties.heading}

יציאת השרת: ${properties.properties.values["server.port"]}
```

המשתנים `main`, `vault` ו-`properties` תואמים לערכי `template-key` בקובצי ה-YAML.

ניתן להשתמש בכל התכונות הנתמכות של FreeMarker כדי לעצב ולהציג את הנתונים.

### 5. יצירת התיעוד

הרץ את הפקודה:

```
mvn installation-guide:generate
```

המסמכים ייווצרו בתיקייה שהוגדרה באמצעות `outputDirectory`.

## שימוש במספר תבניות

ניתן להגדיר מספר רכיבי `<template>` כדי ליצור מסמכים או פורמטי פלט שונים באמצעות אותם נתונים.

דוגמה:

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

כל תבנית מציינת במפורש את קובצי YAML הדרושים לה. ניתן להשתמש באותם קובצי YAML במספר תבניות.

## שילוב במחזור החיים של Maven

המטרה `generate` אינה משויכת כברירת מחדל לאף שלב במחזור החיים של Maven.

ניתן לשייך אותה לשלב באמצעות הגדרת ביצוע בקובץ `pom.xml`:

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

בחר את שלב Maven המתאים לצרכים שלך.

המטרה `scaffold` מיועדת להפעלה ידנית, ואין לשייך אותה אוטומטית למחזור החיים.

## מדריך הגדרות

| פרמטר                               | תיאור                              |
| ----------------------------------- | ---------------------------------- |
| `outputDirectory`                   | התיקייה שבה נשמרים המסמכים שנוצרו. |
| `templates`                         | רשימת הגדרות התבניות.              |
| `templates.template.name`           | שם לזיהוי הגדרת תבנית.             |
| `templates.template.file`           | הנתיב לקובץ תבנית FreeMarker.      |
| `templates.template.output`         | שם הקובץ שנוצר.                    |
| `templates.template.dataFiles`      | קובצי YAML שבהם התבנית משתמשת.     |
| `templates.template.dataFiles.file` | הנתיב לקובץ YAML.                  |

## המלצות

- הפרד בין תוכן התיעוד לבין אופן הצגתו.
- השתמש בקובצי YAML נפרדים כדי לארגן קטגוריות נתונים שונות.
- בחר ערכי `template-key` ברורים וייחודיים.
- הגדר לכל תבנית רק את קובצי YAML הדרושים לה.
- השתמש ב-`${project.basedir}` כדי להגדיר בבירור נתיבים למשאבי הפרויקט.
- שמור את התבניות בתיקייה ייעודית.
- השתמש במספר תבניות כאשר ברצונך ליצור פורמטי פלט שונים מאותם נתונים.

## תרומה לפרויקט

תרומות לפרויקט מתקבלות בברכה.

ניתן לתרום באמצעות דיווח על בעיות, הצעת שיפורים או שליחת Pull Requests.

## רישיון

למידע על תנאי הרישיון, עיין בקובץ `LICENSE` של הפרויקט.

## קישורים

- [מאגר GitHub](https://github.com/mimdal-code/project-doc-maven-plugin)
- [דיווח על בעיה](https://github.com/mimdal-code/project-doc-maven-plugin/issues)