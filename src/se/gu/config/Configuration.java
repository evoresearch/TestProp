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

    public String getExecution() {
        return execution;
    }

    public void setExecution(String execution) {
        this.execution = execution;
    }

    private String execution;

    public String getuUTPairsFile() {
        return uUTPairsFile;
    }

    public void setuUTPairsFile(String uUTPairsFile) {
        this.uUTPairsFile = uUTPairsFile;
    }

    private String uUTPairsFile;

    public int getIndexOfSourceFile() {
        return indexOfSourceFile;
    }

    public void setIndexOfSourceFile(String indexOfSourceFile) {
        this.indexOfSourceFile = Integer.parseInt(indexOfSourceFile);
    }

    public int getIndexOfTargetUUTFile() {
        return indexOfTargetUUTFile;
    }

    public void setIndexOfTargetUUTFile(String indexOfTargetUUTFile) {
        this.indexOfTargetUUTFile = Integer.parseInt(indexOfTargetUUTFile);
    }

    private int indexOfSourceFile,indexOfTargetUUTFile;

    public String getuUTPairsFinalFileWithEditScripts() {
        return uUTPairsFinalFileWithEditScripts;
    }

    public void setuUTPairsFinalFileWithEditScripts(String uUTPairsFinalFileWithEditScripts) {
        this.uUTPairsFinalFileWithEditScripts = uUTPairsFinalFileWithEditScripts;
    }

    private String uUTPairsFinalFileWithEditScripts;
}

