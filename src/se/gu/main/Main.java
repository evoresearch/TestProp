package se.gu.main;

import com.github.gumtreediff.client.Run;
import org.apache.commons.io.FileUtils;
import se.gu.analysis.EditScriptGetter;
import se.gu.config.Configuration;
import se.gu.model.Matches;
import se.gu.utils.LocalExecutionRunner;
import se.gu.utils.Utilities;

import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        try {
            //configuration
            //Read properties file
            Properties properties = new Properties();
            InputStream inputStream = new FileInputStream("config.properties");
            properties.load(inputStream);

            //set directories
            final File analyisDirectory = Utilities.createOutputDirectory(properties.getProperty("AnalysisDirectory"), false);
            //set configuration
            Configuration configuration = getConfiguration(properties, analyisDirectory);
            if (configuration.getExecution().equalsIgnoreCase("ES")) {
                extractEditScripts(configuration);
            } else if (configuration.getExecution().equalsIgnoreCase("PV")) {
                extractVariations(configuration);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Cleans data files, e.g., by removing duplicates
     * @param configuration
     */
    private static void cleanData(Configuration configuration){
        File resultsFile = new File(String.format("%s/testcaseTargetUUTPairMatching.csv", configuration.getAnalysisDirectory()));//here we want to use the
        File finalResultsFile = new File(String.format("%s/testcaseTargetUUTPairMatchingFINAL.csv", configuration.getAnalysisDirectory()));
    }
    private static void extractVariations(Configuration configuration) {
        PrintWriter writer = null;
        PrintWriter finalWriter = null;
        try {

            File mappingsFile = new File(configuration.getuUTPairsFinalFileWithEditScripts());//we are reading the UUTPairs file with edit scripts so that it's easy to tell which ones are matched despite variations in the file
            File resultsFile = new File(String.format("%s/testcaseTargetUUTPairMatching.csv", configuration.getAnalysisDirectory()));//here we want to use the
            File finalResultsFile = new File(String.format("%s/testcaseTargetUUTPairMatchingFINAL.csv", configuration.getAnalysisDirectory()));
            if (resultsFile.exists()) {
                FileUtils.forceDelete(resultsFile);
            }
            if (finalResultsFile.exists()) {
                FileUtils.forceDelete(finalResultsFile);
            }
            TeaCapWriter teaCapWriter = new TeaCapWriter();
            ConcurrentHashMap<String, String> matchedList = new ConcurrentHashMap<>();
            LocalExecutionRunner executionRunner = new LocalExecutionRunner();
            //read test-UUT mappings file
            List<String> lines = FileUtils.readLines(mappingsFile, "UTF-8");
            writer = new PrintWriter(new FileWriter(resultsFile, true));
            writer.printf("%s;preunmatched;postunmatched;allmatched;allUnmatched;matchedPercent;unMatchedPercent;preunmatchedNames;postunmatchedNames;allUnmatchedNames\n", lines.get(0));
            writer.close();
            finalWriter = new PrintWriter(new FileWriter(finalResultsFile, true));
            finalWriter.printf("%s;preunmatched;postunmatched;allmatched;allUnmatched;matchedPercent;unMatchedPercent;preunmatchedNames;postunmatchedNames;allUnmatchedNames\n", lines.get(0));
            finalWriter.close();
            final int maxLine = 2000000;
            int maxFutures = configuration.getMaxThreads();//number of threads runnng concurrently
            int futureCount = 0;
            //for each file pair, analyse variations
            for (int line = 1; line < lines.size(); line++) {
                String lineText = lines.get(line);

                if (futureCount < maxFutures) {
                    VariationExtractor gen = new VariationExtractor(lineText, maxLine, teaCapWriter, resultsFile, finalResultsFile);
                    createFuture(gen, executionRunner);
                    futureCount++;
                }
                if (futureCount == maxFutures) {
                    executionRunner.waitForTaskToFinish();
                    futureCount = 0;
                }

            }
            executionRunner.waitForTaskToFinish();
            executionRunner.shutdown();

            //now print out everything
//            for (String key : matchedList.keySet()) {
//                String matches = matchedList.get(key);
//                writer.printf("%s;%s\n", key, matches);
//                String[] items = matches.split(";");
//                int preUnmatched = Integer.parseInt(items[0].trim());
//                int postUnmatched = Integer.parseInt(items[1].trim());
//                int allMatched = Integer.parseInt(items[2].trim());
//                if (!(preUnmatched == 0 && postUnmatched == 0 && allMatched == 0)) {
//                    finalWriter.printf("%s;%s\n", key, matches);
//                }
//
//            }
            //create a file with only the results without exceptions

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            if (writer != null) {
//                writer.close();
//            }
//            if (finalWriter != null) {
//                finalWriter.close();
//            }
        }
    }

    private static void tryApplicabilityAnalyzer() throws IOException {
        String projectPath = "C:\\Users\\muka\\repos\\featracer";
        String sourceFilePath = "C:\\Users\\muka\\repos\\featracer\\src\\se\\gu\\main\\ProjectReader.java";
        int sourceStart = 93, sourceEnd = 411;
        String targetFilePath = "C:\\Users\\muka\\repos\\featracer\\src\\se\\gu\\main\\ProjectReader.java";
        int targetStart = 93, targetEnd = 411;
        ApplicabilityAnalyzer analyzer = new ApplicabilityAnalyzer(sourceFilePath, sourceStart, sourceEnd, targetFilePath, targetStart, targetEnd);
        Matches stateMatches = analyzer.state_level_match();
        String results = String.format("%d;%d;%d;%d", analyzer.getPre_unmatch_clone().size(),
                analyzer.getPost_unmatch_clone().size(), stateMatches.getAllOrigin().size(), stateMatches.getAllClones().size());


        System.out.printf("%s vs \n%s\n::%s\n", sourceFilePath, targetFilePath, results);
    }

    private static Configuration getConfiguration(Properties properties, File analyisDirectory) {
        Configuration configuration = Configuration.getInstance();
        configuration.setAnalysisDirectory(analyisDirectory);
        configuration.setMappingsFile(properties.getProperty("MappingsFile"));
        configuration.setMaxThreads(properties.getProperty("MaxThreads"));
        configuration.setExecution(properties.getProperty("Execution"));
        configuration.setuUTPairsFile(properties.getProperty("UUTPairsFile"));
        configuration.setIndexOfSourceFile(properties.getProperty("IndexOfSourceFile"));
        configuration.setIndexOfTargetUUTFile(properties.getProperty("IndexOfTargetUUTFile"));
        configuration.setuUTPairsFinalFileWithEditScripts(properties.getProperty("UUTPairsFinalFileWithEditScripts"));
        return configuration;
    }

    private static void extractEditScripts(Configuration configuration) {

        LocalExecutionRunner executionRunner = new LocalExecutionRunner();
        try {
            File pairsFile = new File(configuration.getuUTPairsFile());

            List<String> lines = FileUtils.readLines(pairsFile, "UTF-8");
            File resultsFile = new File(configuration.getMappingsFile());
//            if (resultsFile.exists()) {
//                FileUtils.forceDelete(resultsFile);
//            }
//            PrintWriter writer = new PrintWriter(new FileWriter(resultsFile, true));
//            //write header
//            writer.printf("lineIndex;%s;editscript\n", lines.get(0));
//            writer.close();
            //ConcurrentHashMap<String, Integer> editScripts = new ConcurrentHashMap<>(); //store pairs of uuts and their editscript lengths
            TeaCapWriter teaCapWriter = new TeaCapWriter();
            //first get unique file pairs, then for each
            //now go through all UUT file pairs and
            Run.initGenerators(); // registers the available parsers
            int size = lines.size();
            //we are going to use threads
            int maxFutures = 40;//number of threads runnng concurrently
            int futureCount = 0;
            //try (ProgressBar pb = new ProgressBar("Analysing diffs", size)) {
            for (int line = 1; line < size; line++) {
                if(line<344436){
                    continue;
                }
                //pb.step();
                if (futureCount < maxFutures) {
                    EditScriptGetter gen = new EditScriptGetter(lines.get(line), line, teaCapWriter, resultsFile,configuration);
                    createFuture(gen, executionRunner);
                    futureCount++;
                }
                if (futureCount == maxFutures) {
                    executionRunner.waitForTaskToFinish();
                    futureCount = 0;
                }


            }
            executionRunner.waitForTaskToFinish();
            executionRunner.shutdown();
            // }

            //now print out everything
//            for (String key : editScripts.keySet()) {
//                writer.printf("%s;%d\n", key, editScripts.get(key));
//            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            if(writer!=null) {
//                writer.close();
//            }
        }
    }

    private static void createFuture(Runnable task, LocalExecutionRunner executionRunner) {
        executionRunner.addFuture(executionRunner.submit(task));
    }
}
