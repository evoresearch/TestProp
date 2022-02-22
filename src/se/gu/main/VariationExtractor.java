package se.gu.main;



import se.gu.model.Matches;
import se.gu.model.Token;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VariationExtractor implements Runnable {
    public VariationExtractor(String lineText, int maxLine, TeaCapWriter teaCapWriter, File resultsFile, File finalResultsFile) {
        this.lineText = lineText;
        this.maxLine = maxLine;
        this.teaCapWriter=teaCapWriter;
        this.resultsFile = resultsFile;
        this.finalResultsFile = finalResultsFile;
        //this.matchedList = matchedList;
    }

    private String lineText;
    private int maxLine;
    private TeaCapWriter teaCapWriter;
    private File resultsFile, finalResultsFile;
    //ConcurrentHashMap<String, String> matchedList;

    @Override
    public void run() {
        try {
            String[] items = lineText.split(";");
            String sourceProject = items[2];
            String sourceFilePath = items[6];
            String sourceProjectPath = getProjectPath(sourceProject, sourceFilePath);
            String sourceMethodName = items[8];
            int sourceMethodStart = Integer.parseInt(items[9]);
            int sourceMethodEnd = Integer.parseInt(items[10]);
            int sourceCloneStart = Integer.parseInt(items[11]);
            int sourceCloneEnd = Integer.parseInt(items[12]);
            sourceMethodEnd = sourceMethodEnd > maxLine ? sourceCloneEnd : sourceMethodEnd;

            String targetProject = items[13];
            String targetFilePath = items[14];
            String targetProjectPath = getProjectPath(targetProject,targetFilePath);
            String targetMethodName = items[16];
            int targetMethodStart = Integer.parseInt(items[17]);
            int targetMethodEnd = Integer.parseInt(items[18]);
            int targetCloneStart = Integer.parseInt(items[19]);
            int targetCloneEnd = Integer.parseInt(items[20]);
            // TODO: What should this be? maxLine = 2000000 -> this messes with cases where the method extends the clone
            targetMethodEnd = targetMethodEnd > maxLine ? targetCloneEnd : targetMethodEnd;
            ApplicabilityAnalyzer grafter = new ApplicabilityAnalyzer(sourceFilePath, sourceMethodStart, sourceMethodEnd, targetFilePath, targetMethodStart, targetMethodEnd);
            Matches stateMatches = grafter.state_level_match();
            HashSet<Token> allUnMatchedTokens = new HashSet<>();
            allUnMatchedTokens.addAll(grafter.getPre_unmatch_clone());
            allUnMatchedTokens.addAll(grafter.getPost_unmatch_clone());
            int preUnMatched = grafter.getPre_unmatch_clone().size();
            int postUnMatched = grafter.getPost_unmatch_clone().size();
            int allMatched = stateMatches.getAllClones().size();
            int allUnMatched = allUnMatchedTokens.size();
            double sum = allMatched+allUnMatched;
            double allM = allMatched,allUM = allUnMatched;
            double matchedPercent = (allM/sum)*100;
            double unMatchedPercent = (allUM/sum)*100;

            String results = String.format("%d;%d;%d;%d;%.2f;%.2f;%s;%s;%s", preUnMatched,
                    postUnMatched, allMatched,allUnMatched,matchedPercent,unMatchedPercent,getVariableNames(grafter.getPre_unmatch_clone()),getVariableNames(grafter.getPost_unmatch_clone()),getVariableNames(allUnMatchedTokens));
            //matchedList.put(lineText, results);
            teaCapWriter.writeVariationToFile(lineText,results,resultsFile);
            if (!(preUnMatched == 0 && postUnMatched == 0 && allMatched == 0)) {
                teaCapWriter.writeFinalVariationToFile(lineText,results,finalResultsFile);
            }

            //System.out.printf("%s::%s\n",lineText,results);
        } catch (Exception ex) {
            System.out.printf("%s\n", lineText);
            ex.printStackTrace();
        }
    }
    private String getVariableNames(HashSet<Token> tokens){
       return tokens.parallelStream().map(Token::getName).distinct().collect(Collectors.joining(","));
    }

    private String getProjectPath(String sourceProject, String sourceFilePath) {
        String sourceProjectPath = sourceFilePath.replace("\\","/");
        sourceProjectPath = sourceProjectPath.replace(sourceProject,"::").split("::")[0];
        sourceProjectPath=String.format("%s%s",sourceProjectPath, sourceProject);
        return sourceProjectPath;
    }
}
