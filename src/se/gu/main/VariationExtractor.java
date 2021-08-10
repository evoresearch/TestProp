package se.gu.main;



import se.gu.model.Matches;

import java.util.concurrent.ConcurrentHashMap;

public class VariationExtractor implements Runnable {
    public VariationExtractor(String lineText, int maxLine, ConcurrentHashMap<String, String> matchedList) {
        this.lineText = lineText;
        this.maxLine = maxLine;
        this.matchedList = matchedList;
    }

    private String lineText;
    private int maxLine;
    ConcurrentHashMap<String, String> matchedList;

    @Override
    public void run() {
        try {
            String[] items = lineText.split(";");
            String sourceProject = items[3];
            String sourceFilePath = items[7];
            String sourceProjectPath = getProjectPath(sourceProject, sourceFilePath);
            String sourceMethodName = items[9];
            int sourceMethodStart = Integer.parseInt(items[10]);
            int sourceMethodEnd = Integer.parseInt(items[11]);
            int sourceCloneStart = Integer.parseInt(items[12]);
            int sourceCloneEnd = Integer.parseInt(items[13]);
            sourceMethodEnd = sourceMethodEnd > maxLine ? sourceCloneEnd : sourceMethodEnd;

            String targetProject = items[14];
            String targetFilePath = items[15];
            String targetProjectPath = getProjectPath(targetProject,targetFilePath);
            String targetMethodName = items[17];
            int targetMethodStart = Integer.parseInt(items[18]);
            int targetMethodEnd = Integer.parseInt(items[19]);
            int targetCloneStart = Integer.parseInt(items[20]);
            int targetCloneEnd = Integer.parseInt(items[21]);
            targetMethodEnd = targetMethodEnd > maxLine ? targetCloneEnd : targetMethodEnd;
            ApplicabilityAnalyzer grafter = new ApplicabilityAnalyzer(sourceFilePath, sourceMethodStart, sourceMethodEnd, targetFilePath, targetMethodStart, targetMethodEnd,sourceProjectPath,targetProjectPath);
            Matches stateMatches = grafter.state_level_match();
            String results = String.format("%d;%d;%d", grafter.getPre_unmatch_clone().size(),
                    grafter.getPost_unmatch_clone().size(), stateMatches.getAllClones().size());
            matchedList.put(lineText, results);

            System.out.printf("%s::%s\n",lineText,results);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getProjectPath(String sourceProject, String sourceFilePath) {
        String sourceProjectPath = sourceFilePath.replace("\\","/");
        sourceProjectPath = sourceProjectPath.replace(sourceProject,"::").split("::")[0];
        sourceProjectPath=String.format("%s%s",sourceProjectPath, sourceProject);
        return sourceProjectPath;
    }
}
