package com.github.mimdal.installationguide.service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class YamlDataLoader {

    public Map<String, Object> load(Path file) throws IOException {
        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(new SafeConstructor(options));

        Object data;

        try (Reader reader = Files.newBufferedReader(file)) {
            data = yaml.load(reader);
        }

        if (data == null) {
            throw new IllegalArgumentException(
                    "YAML document is empty: " + file
            );
        }

        if (!(data instanceof Map<?, ?>)) {
            throw new IllegalArgumentException(
                    "YAML root must be a mapping: " + file
            );
        }

        Map<?, ?> source = (Map<?, ?>) data;
        Map<String, Object> values = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : source.entrySet()) {
            Object key = entry.getKey();

            if (!(key instanceof String)) {
                throw new IllegalArgumentException(
                        "YAML root mapping keys must be strings: " + file
                );
            }

            values.put((String) key, entry.getValue());
        }

        Object templateKeyValue = values.remove("template-key");

        if (!(templateKeyValue instanceof String)) {
            throw new IllegalArgumentException(
                    "YAML root must contain a string 'template-key': " + file
            );
        }

        String templateKey = ((String) templateKeyValue).trim();

        if (templateKey.isEmpty()) {
            throw new IllegalArgumentException(
                    "YAML 'template-key' must not be empty: " + file
            );
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put(templateKey, values);

        return result;
    }
}