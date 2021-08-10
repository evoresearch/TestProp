package se.gu.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Utilities {
    public static File createOutputDirectory(String outputDirectory, boolean cleanDirectoryIfExists) throws IOException {


        File dotFilesDirectory = new File(outputDirectory);
        if (!dotFilesDirectory.exists()) {
            FileUtils.forceMkdir(dotFilesDirectory);
        } else {
            if(cleanDirectoryIfExists){
                FileUtils.cleanDirectory(dotFilesDirectory);
            }
        }


        return dotFilesDirectory;
    }
}
