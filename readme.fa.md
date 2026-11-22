# Project Documentation Maven Plugin

English | Français | Deutsch | **فارسی** | العربية | עברית | 简体中文

افزونه‌ای برای Maven که مستندات پروژه را به‌صورت خودکار و با استفاده از قالب‌ها و فایل‌های داده YAML تولید می‌کند.

## معرفی

افزونه `project-doc-maven-plugin` محتوای مستندات را از نحوه نمایش آن جدا می‌کند. این افزونه از قالب‌های FreeMarker و فایل‌های YAML به‌عنوان منابع داده استفاده می‌کند تا مستنداتی متناسب با نیازهای پروژه شما تولید کند.

می‌توانید چندین قالب تعریف کنید. هر قالب می‌تواند فایل‌های YAML و فایل خروجی مخصوص به خود را داشته باشد.

## امکانات

- تولید مستندات با استفاده از قالب‌های FreeMarker.
- استفاده از فایل‌های YAML به‌عنوان منابع داده.
- پشتیبانی از چندین قالب و فرمت خروجی.
- اتصال چندین فایل YAML به یک قالب.
- استفاده از `template-key` برای تعیین نام متغیرهای قابل دسترس در قالب‌ها.
- تولید فایل‌ها در یک پوشه خروجی قابل تنظیم.
- دستور `scaffold` برای ایجاد ساختار اولیه مستندات.
- استفاده مجدد از فایل‌های YAML یکسان در چندین قالب.

## پیش‌نیازها

- Java 17 یا بالاتر.
- Apache Maven نسخه 3.6.3 یا بالاتر.

## نصب

افزونه را به بخش `<build><plugins>` در فایل `pom.xml` اضافه کنید:

```
<plugin>
    <groupId>com.github.mimdal</groupId>
    <artifactId>project-doc-maven-plugin</artifactId>
    <version>${plugin.version}</version>
</plugin>
```

مقدار `${plugin.version}` را با نسخه موردنظر افزونه جایگزین کنید.

## نحوه استفاده

این افزونه دو هدف Maven ارائه می‌دهد:

| هدف                           | توضیحات                                                               |
| ----------------------------- | --------------------------------------------------------------------- |
| `installation-guide:scaffold` | فایل‌های اولیه مستندات پروژه را ایجاد می‌کند.                         |
| `installation-guide:generate` | مستندات را بر اساس قالب‌ها و فایل‌های YAML پیکربندی‌شده تولید می‌کند. |

### ۱. ایجاد ساختار اولیه

دستور زیر را اجرا کنید:

```
mvn installation-guide:scaffold
```

این دستور فایل‌های اولیه را در پوشه `project-doc/` پروژه شما ایجاد می‌کند.

رفتار این دستور:

- فقط فایل‌هایی را ایجاد می‌کند که از قبل وجود ندارند.
- فایل‌های موجود را هرگز بازنویسی نمی‌کند.
- اجرای مجدد آن بی‌خطر است.
- فایل‌های ایجادشده و فایل‌های ردشده را گزارش می‌دهد.
- فایل `pom.xml` را تغییر نمی‌دهد.
- مستنداتی تولید نمی‌کند.

### ۲. پیکربندی تولید مستندات

پیکربندی زیر را به افزونه در فایل `pom.xml` اضافه کنید:

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

هر عنصر `<template>` یک سند برای تولید تعریف می‌کند:

| عنصر        | توضیحات                                    |
| ----------- | ------------------------------------------ |
| `name`      | نامی برای شناسایی پیکربندی قالب.           |
| `file`      | مسیر فایل قالب FreeMarker.                 |
| `output`    | نام فایل تولیدشده در پوشه خروجی.           |
| `dataFiles` | فهرست فایل‌های YAML مورد استفاده این قالب. |

مسیرهای نسبی از پوشه اصلی پروژه Maven (`${project.basedir}`) محاسبه می‌شوند. همچنین می‌توانید از مسیرهای مطلق استفاده کنید. توصیه می‌شود برای مشخص بودن مسیرها، در پیکربندی از `${project.basedir}` استفاده کنید.

مسیر خروجی نسبت به `outputDirectory` محاسبه می‌شود.

### ۳. آماده‌سازی فایل‌های YAML

هر فایل YAML باید یک ویژگی الزامی با نام `template-key` در سطح ریشه داشته باشد.

این ویژگی نام متغیری را تعیین می‌کند که در قالب FreeMarker در دسترس خواهد بود.

نمونه: `doc-main.yaml`

```
template-key: main

document:
  title: "راهنمای نصب"
  description: "راهنمای نصب پروژه"
```

نمونه: `doc-vault.yaml`

```
template-key: vault

vault:
  heading: "پیکربندی مخزن"
  enabled: true
```

نمونه: `doc-properties.yaml`

```
template-key: properties

properties:
  heading: "پیکربندی ویژگی‌ها"
  values:
    server.port: 8080
    logging.level: INFO
```

در این مثال، سه فایل YAML متغیرهای زیر را در اختیار قالب قرار می‌دهند:

| فایل YAML             | `template-key` | متغیر FreeMarker |
| --------------------- | -------------- | ---------------- |
| `doc-main.yaml`       | `main`         | `main`           |
| `doc-vault.yaml`      | `vault`        | `vault`          |
| `doc-properties.yaml` | `properties`   | `properties`     |

**نکات مهم:**

- `template-key` باید در سطح ریشه هر فایل YAML وجود داشته باشد.
- مقدار آن باید یک رشته غیرخالی باشد.
- مقادیر `template-key` در میان فایل‌های YAML متعلق به یک قالب باید یکتا باشند.
- نام فایل YAML هیچ تأثیری بر نام متغیر FreeMarker ندارد.
- ویژگی `template-key` برای شناسایی داده‌ها استفاده می‌شود و در شیء داده‌ای ارسال‌شده به قالب قرار نمی‌گیرد.
- نیازی نیست برای هر فایل YAML یک نام مستعار با عنصر Maven به نام `<name>` تعریف کنید.

### ۴. استفاده از داده‌ها در قالب FreeMarker

داده‌های هر فایل YAML از طریق متغیری قابل دسترس هستند که مقدار آن توسط `template-key` تعیین شده است.

نمونه:

```
# ${main.document.title}

${main.document.description}

## ${vault.vault.heading}

وضعیت: ${vault.vault.enabled?string("فعال", "غیرفعال")}

## ${properties.properties.heading}

پورت سرور: ${properties.properties.values["server.port"]}
```

متغیرهای `main`، `vault` و `properties` متناظر با مقادیر `template-key` در فایل‌های YAML هستند.

برای قالب‌بندی و نمایش داده‌ها می‌توانید از تمام قابلیت‌های پشتیبانی‌شده توسط FreeMarker استفاده کنید.

### ۵. تولید مستندات

دستور زیر را اجرا کنید:

```
mvn installation-guide:generate
```

مستندات در پوشه‌ای که با `outputDirectory` تنظیم شده است تولید می‌شوند.

## استفاده از چندین قالب

می‌توانید چندین عنصر `<template>` تعریف کنید تا با استفاده از داده‌های یکسان، اسناد یا فرمت‌های خروجی متفاوتی تولید کنید.

نمونه:

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

هر قالب به‌صورت صریح فایل‌های YAML موردنیاز خود را مشخص می‌کند. یک فایل YAML می‌تواند در چندین قالب استفاده مجدد شود.

## اتصال به چرخه حیات Maven

هدف `generate` به‌صورت پیش‌فرض به هیچ مرحله‌ای از چرخه حیات Maven متصل نیست.

می‌توانید با تعریف یک اجرا در فایل `pom.xml` آن را به یک مرحله متصل کنید:

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

مرحله Maven را متناسب با نیاز خود انتخاب کنید.

هدف `scaffold` برای اجرای دستی در نظر گرفته شده است و نباید به‌صورت خودکار به چرخه حیات متصل شود.

## مرجع پیکربندی

| پارامتر                             | توضیحات                                          |
| ----------------------------------- | ------------------------------------------------ |
| `outputDirectory`                   | پوشه‌ای که مستندات تولیدشده در آن ذخیره می‌شوند. |
| `templates`                         | فهرست پیکربندی قالب‌ها.                          |
| `templates.template.name`           | نامی برای شناسایی یک پیکربندی قالب.              |
| `templates.template.file`           | مسیر فایل قالب FreeMarker.                       |
| `templates.template.output`         | نام فایل تولیدشده.                               |
| `templates.template.dataFiles`      | فایل‌های YAML مورد استفاده قالب.                 |
| `templates.template.dataFiles.file` | مسیر یک فایل YAML.                               |

## توصیه‌ها

- محتوای مستندات را از نحوه نمایش آن جدا نگه دارید.
- برای سازمان‌دهی دسته‌های مختلف داده، از فایل‌های YAML جداگانه استفاده کنید.
- برای `template-key` مقادیر معنادار و یکتا انتخاب کنید.
- فقط فایل‌های YAML موردنیاز هر قالب را در آن تعریف کنید.
- برای مشخص کردن مسیر منابع پروژه از `${project.basedir}` استفاده کنید.
- قالب‌ها را در یک پوشه اختصاصی نگهداری کنید.
- اگر می‌خواهید از داده‌های یکسان چندین فرمت خروجی تولید کنید، از چندین قالب استفاده کنید.

## مشارکت در پروژه

از مشارکت شما استقبال می‌شود.

می‌توانید با گزارش مشکلات، پیشنهاد بهبود یا ارسال Pull Request در توسعه پروژه مشارکت کنید.

## مجوز

برای اطلاع از شرایط مجوز، فایل `LICENSE` پروژه را بررسی کنید.

## پیوندها

- [مخزن GitHub](https://github.com/mimdal-code/project-doc-maven-plugin)
- [گزارش مشکل](https://github.com/mimdal-code/project-doc-maven-plugin/issues)
