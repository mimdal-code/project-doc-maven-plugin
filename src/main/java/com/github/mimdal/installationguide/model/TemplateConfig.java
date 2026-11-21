package com.github.mimdal.installationguide.model;

import java.util.ArrayList;
import java.util.List;

public class TemplateConfig {

    private String name;
    private String file;
    private String output;
    private List<DataFileConfig> dataFiles = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<DataFileConfig> getDataFiles() {
        return dataFiles;
    }

    public void setDataFiles(List<DataFileConfig> dataFiles) {
        this.dataFiles = dataFiles;
    }
}
