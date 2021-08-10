package se.gu.config;

import java.io.File;

public class Configuration {
    private static Configuration ourInstance = new Configuration();

    public static Configuration getInstance() {
        return ourInstance;
    }
    private String mappingsFile;

    public String getMappingsFile() {
        return mappingsFile;
    }

    public void setMappingsFile(String mappingsFile) {
        this.mappingsFile = mappingsFile;
    }
    private File analysisDirectory;
    public File getAnalysisDirectory() {
        return analysisDirectory;
    }

    public void setAnalysisDirectory(File analysisDirectory) {
        this.analysisDirectory = analysisDirectory;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(String maxThreads) {
        this.maxThreads = Integer.parseInt(maxThreads.trim());
    }

    private int maxThreads;
}

