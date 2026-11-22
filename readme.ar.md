# Project Documentation Maven Plugin

English | Français | Deutsch | فارسی | **العربية** | עברית | 简体中文

إضافة Maven لتوليد توثيق المشاريع تلقائيًا باستخدام القوالب وملفات بيانات YAML.

## نظرة عامة

تفصل إضافة `project-doc-maven-plugin` محتوى التوثيق عن طريقة عرضه. وتستخدم قوالب FreeMarker وملفات YAML كمصادر للبيانات، بهدف إنشاء مستندات تلبي احتياجات مشروعك.

يمكنك تعريف عدة قوالب، بحيث يكون لكل قالب ملفات YAML وملف إخراج خاص به.

## الميزات

- توليد التوثيق باستخدام قوالب FreeMarker.
- استخدام ملفات YAML كمصادر للبيانات.
- دعم عدة قوالب وتنسيقات إخراج.
- ربط عدة ملفات YAML بقالب واحد.
- استخدام `template-key` لتحديد أسماء المتغيرات المتاحة داخل القوالب.
- إنشاء الملفات في مجلد إخراج قابل للتهيئة.
- توفير الأمر `scaffold` لإنشاء البنية الأولية للتوثيق.
- إعادة استخدام ملفات YAML نفسها في عدة قوالب.

## المتطلبات

- Java 17 أو إصدار أحدث.
- Apache Maven 3.6.3 أو إصدار أحدث.

## التثبيت

أضف الإضافة إلى قسم `<build><plugins>` في ملف `pom.xml`:

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
</plugin>
```

استبدل `${plugin.version}` بإصدار الإضافة الذي تريد استخدامه.

## الاستخدام

توفر الإضافة هدفي Maven التاليين:

| الهدف                         | الوصف                                                         |
| ----------------------------- | ------------------------------------------------------------- |
| `installation-guide:scaffold` | ينشئ الملفات الأولية لتوثيق المشروع.                          |
| `installation-guide:generate` | يولّد المستندات باستخدام القوالب وملفات YAML التي تم إعدادها. |

### 1. إنشاء البنية الأولية

نفّذ الأمر التالي:

```
mvn installation-guide:scaffold
```

ينشئ هذا الأمر الملفات الأولية داخل المجلد `project-doc/` في مشروعك.

السلوك:

- ينشئ الملفات التي لا توجد مسبقًا فقط.
- لا يستبدل الملفات الموجودة أبدًا.
- يمكن تشغيله عدة مرات بأمان.
- يعرض الملفات التي تم إنشاؤها والملفات التي تم تخطيها.
- لا يعدّل ملف `pom.xml`.
- لا يولّد أي مستندات.

### 2. إعداد التوليد

أضف الإعداد التالي إلى تعريف الإضافة في ملف `pom.xml`:

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

يعرّف كل عنصر `<template>` مستندًا مطلوبًا توليده:

| العنصر      | الوصف                                      |
| ----------- | ------------------------------------------ |
| `name`      | اسم يعرّف إعداد القالب.                    |
| `file`      | مسار ملف قالب FreeMarker.                  |
| `output`    | اسم الملف الناتج داخل مجلد الإخراج.        |
| `dataFiles` | قائمة ملفات YAML التي يستخدمها هذا القالب. |

تُحل المسارات النسبية انطلاقًا من مجلد مشروع Maven (`${project.basedir}`). ويمكنك أيضًا استخدام المسارات المطلقة. يُنصح باستخدام `${project.basedir}` في الإعدادات لتحديد المسارات بوضوح.

يكون مسار الإخراج نسبيًا إلى `outputDirectory`.

### 3. إعداد ملفات YAML

يجب أن يحتوي كل ملف YAML على خاصية إلزامية باسم `template-key` في المستوى الجذر.

تحدد هذه الخاصية اسم المتغير الذي سيكون متاحًا داخل قالب FreeMarker.

مثال: `doc-main.yaml`

```
template-key: main

document:
  title: "دليل التثبيت"
  description: "دليل تثبيت المشروع"
```

مثال: `doc-vault.yaml`

```
template-key: vault

vault:
  heading: "إعداد الخزنة"
  enabled: true
```

مثال: `doc-properties.yaml`

```
template-key: properties

properties:
  heading: "إعداد الخصائص"
  values:
    server.port: 8080
    logging.level: INFO
```

في هذا المثال، توفر ملفات YAML الثلاثة المتغيرات التالية:

| ملف YAML              | `template-key` | متغير FreeMarker |
| --------------------- | -------------- | ---------------- |
| `doc-main.yaml`       | `main`         | `main`           |
| `doc-vault.yaml`      | `vault`        | `vault`          |
| `doc-properties.yaml` | `properties`   | `properties`     |

**ملاحظات مهمة:**

- يجب أن تكون `template-key` موجودة في المستوى الجذر لكل ملف YAML.
- يجب أن تكون قيمتها سلسلة نصية غير فارغة.
- يجب أن تكون قيم `template-key` فريدة بين ملفات YAML المرتبطة بالقالب نفسه.
- لا يؤثر اسم ملف YAML في اسم متغير FreeMarker.
- تُستخدم الخاصية `template-key` لتحديد بيانات الملف، ولا تُضمّن في كائن البيانات المرسل إلى القالب.
- لا تحتاج إلى تعريف اسم مستعار لكل ملف YAML باستخدام عنصر Maven باسم `<name>`.

### 4. استخدام البيانات داخل قالب FreeMarker

يمكن الوصول إلى بيانات كل ملف YAML من خلال المتغير الذي تحدده قيمة `template-key` الخاصة به.

مثال:

```
# ${main.document.title}

${main.document.description}

## ${vault.vault.heading}

الحالة: ${vault.vault.enabled?string("مفعّل", "معطّل")}

## ${properties.properties.heading}

منفذ الخادم: ${properties.properties.values["server.port"]}
```

تتوافق المتغيرات `main` و`vault` و`properties` مع قيم `template-key` في ملفات YAML.

يمكنك استخدام جميع الميزات التي يدعمها FreeMarker لتنسيق البيانات وعرضها.

### 5. توليد التوثيق

نفّذ الأمر التالي:

```
mvn installation-guide:generate
```

سيتم إنشاء المستندات في المجلد المحدد بواسطة `outputDirectory`.

## استخدام عدة قوالب

يمكنك تعريف عدة عناصر `<template>` لإنشاء مستندات أو تنسيقات إخراج مختلفة باستخدام البيانات نفسها.

مثال:

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

يحدد كل قالب صراحةً ملفات YAML التي يحتاج إليها. ويمكن إعادة استخدام ملفات YAML نفسها في عدة قوالب.

## التكامل مع دورة حياة Maven

لا يرتبط الهدف `generate` افتراضيًا بأي مرحلة من مراحل دورة حياة Maven.

يمكنك ربطه بمرحلة معينة عن طريق إعداد تنفيذ داخل ملف `pom.xml`:

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

اختر مرحلة Maven التي تناسب احتياجاتك.

الهدف `scaffold` مخصص للتشغيل اليدوي، ولا ينبغي ربطه تلقائيًا بدورة الحياة.

## مرجع الإعدادات

| المعامل                             | الوصف                                    |
| ----------------------------------- | ---------------------------------------- |
| `outputDirectory`                   | المجلد الذي تُحفظ فيه المستندات الناتجة. |
| `templates`                         | قائمة إعدادات القوالب.                   |
| `templates.template.name`           | اسم يعرّف إعداد قالب.                    |
| `templates.template.file`           | مسار ملف قالب FreeMarker.                |
| `templates.template.output`         | اسم الملف الناتج.                        |
| `templates.template.dataFiles`      | ملفات YAML التي يستخدمها القالب.         |
| `templates.template.dataFiles.file` | مسار ملف YAML.                           |

## أفضل الممارسات

- افصل محتوى التوثيق عن طريقة عرضه.
- استخدم ملفات YAML منفصلة لتنظيم فئات البيانات المختلفة.
- اختر قيمًا واضحة وفريدة لـ `template-key`.
- أدرج في كل قالب ملفات YAML التي يحتاج إليها فقط.
- استخدم `${project.basedir}` لتحديد مسارات موارد المشروع بوضوح.
- احتفظ بالقوالب داخل مجلد مخصص.
- استخدم عدة قوالب عندما تحتاج إلى إنشاء تنسيقات إخراج مختلفة من البيانات نفسها.

## المساهمة

نرحب بالمساهمات.

يمكنك المساهمة من خلال الإبلاغ عن المشكلات، أو اقتراح التحسينات، أو إرسال طلبات السحب (Pull Requests).

## الترخيص

راجع ملف `LICENSE` في المشروع للاطلاع على شروط الترخيص.

## الروابط

- [مستودع GitHub](https://github.com/mimdal-code/project-doc-maven-plugin)
- [الإبلاغ عن مشكلة](https://github.com/mimdal-code/project-doc-maven-plugin/issues)