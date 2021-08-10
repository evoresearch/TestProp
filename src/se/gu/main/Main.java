package se.gu.main;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.client.Run;
import com.github.gumtreediff.gen.TreeGenerators;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;
import se.gu.analysis.EditScriptGetter;
import se.gu.config.Configuration;
import se.gu.model.Matches;
import se.gu.utils.LocalExecutionRunner;
import se.gu.utils.Utilities;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        //extractEditScripts();
        extractVariations();
    }

    private static void extractVariations() {
        PrintWriter writer = null;
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
            File mappingsFile = new File(configuration.getMappingsFile());//we are reading the UUTPairs file with edit scripts so that it's easy to tell which ones are matched despite variations in the file
            File resultsFile = new File(String.format("%s/testcaseTargetUUTPairMatching.csv", analyisDirectory));//here we want to use the
            if (resultsFile.exists()) {
                FileUtils.forceDelete(resultsFile);
            }

            ConcurrentHashMap<String, String> matchedList = new ConcurrentHashMap<>();
            LocalExecutionRunner executionRunner = new LocalExecutionRunner();
            //read test-UUT mappings file
            List<String> lines = FileUtils.readLines(mappingsFile, "UTF-8");
            writer = new PrintWriter(new FileWriter(resultsFile, true));
            writer.printf("%s;preunmatched;postunmatched;allmatched\n", lines.get(0));
            final int maxLine = 2000000;
            int maxFutures = configuration.getMaxThreads();//number of threads runnng concurrently
            int futureCount = 0;
            //for each file pair, analyse variations
            for (int line = 1; line < lines.size(); line++) {
                String lineText = lines.get(line);

                if (futureCount < maxFutures) {
                    VariationExtractor gen = new VariationExtractor(lineText, maxLine, matchedList);
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
            for (String key : matchedList.keySet()) {
                writer.printf("%s;%s\n", key, matchedList.get(key));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private static void tryApplicabilityAnalyzer() throws IOException {
        String projectPath = "C:\\Users\\muka\\repos\\featracer";
        String sourceFilePath = "C:\\Users\\muka\\repos\\featracer\\src\\se\\gu\\main\\ProjectReader.java";
        int sourceStart = 93, sourceEnd = 411;
        String targetFilePath = "C:\\Users\\muka\\repos\\featracer\\src\\se\\gu\\main\\ProjectReader.java";
        int targetStart = 93, targetEnd = 411;
        ApplicabilityAnalyzer analyzer = new ApplicabilityAnalyzer(sourceFilePath, sourceStart, sourceEnd, targetFilePath, targetStart, targetEnd, projectPath, projectPath);
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

        return configuration;
    }

    private static void extractEditScripts() {
        PrintWriter writer = null;
        LocalExecutionRunner executionRunner = new LocalExecutionRunner();
        try {
            File pairsFile = new File("C:/testpropagation/clones/studyresults/testCaseTargetUUTPairsMethodNames.csv");

            List<String> lines = FileUtils.readLines(pairsFile, "UTF-8");
            File resultsFile = new File("C:/testpropagation/clones/studyresults/testCaseTargetUUTPairsEditScripts.csv");
            if (resultsFile.exists()) {
                FileUtils.forceDelete(resultsFile);
            }
            writer = new PrintWriter(new FileWriter(resultsFile, true));
            //write header
            writer.printf("lineIndex;%s;editscript\n", lines.get(0));
            ConcurrentHashMap<String, Integer> editScripts = new ConcurrentHashMap<>(); //store pairs of uuts and their editscript lengths
            //first get unique file pairs, then for each
            //now go through all UUT file pairs and
            Run.initGenerators(); // registers the available parsers
            int size = lines.size();
            //we are going to use threads
            int maxFutures = 30;//number of threads runnng concurrently
            int futureCount = 0;
            //try (ProgressBar pb = new ProgressBar("Analysing diffs", size)) {
            for (int line = 1; line < size; line++) {
                //pb.step();
                if (futureCount < maxFutures) {
                    EditScriptGetter gen = new EditScriptGetter(lines.get(line), line, editScripts);
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
            for (String key : editScripts.keySet()) {
                writer.printf("%s;%d\n", key, editScripts.get(key));
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            writer.close();
        }
    }

    private static void createFuture(Runnable task, LocalExecutionRunner executionRunner) {
        executionRunner.addFuture((Future<?>) executionRunner.submit(task));
    }
}
