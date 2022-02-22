package se.gu.main;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import se.gu.analysis.*;
import se.gu.model.*;
import se.gu.utils.FileUtiltities;
import se.gu.utils.JParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class analyzes two code clones to find any variations in their variables and assess whether a test case
 * from the source clone can be applied to the target clone
 * @author Mukelabai Mukelabai
 * @date 2021-08-10
 *
 * Code is inspired by work from Tianyi Zhang
 */
public class ApplicabilityAnalyzer {
    private String sourceCode, targetCode,sourceProjectPath,targetProjectPath;
    //source clone (origin of test case)
    public Clone sourceClone;
    private HashSet<Token> prestate1;
    private HashSet<Token> poststate1;
    private HashSet<Token> allstate1;
    private HashMap<String, HashSet<Token>> all1;
    private HashMap<String, HashSet<Token>> pre1;
    private HashMap<String, HashSet<Token>> post1;
    private int orign_start;
    private int orign_end;
    private CloneVisitor cv1;

    //target clone (target of test case)
    public Clone targetClone;
    private HashSet<Token> prestate2;
    private HashSet<Token> poststate2;
    private HashSet<Token> allstate2;
    private HashMap<String, HashSet<Token>> all2;
    private HashMap<String, HashSet<Token>> pre2;
    private HashMap<String, HashSet<Token>> post2;
    private int clone_start;
    private int clone_end;
    private CloneVisitor cv2;

    //extra fields to keep track of

    private CloneMatcher pre_cm;
    private Matches prematch;
    private HashSet<Token> pre_unmatch_clone;
    private CloneMatcher post_cm;
    public Matches postmatch;
    private HashSet<Token> post_unmatch_clone;

    private int methodStart;
    public boolean isMethodEnd;

    public CloneMatcher getPre_cm() {return pre_cm;	}
    public Matches getPrematch() {return prematch;}
    public HashSet<Token> getPre_unmatch_clone() {
        return pre_unmatch_clone;
    }
    public CloneMatcher getPost_cm() {
        return post_cm;
    }
    public HashSet<Token> getPost_unmatch_clone() {
        return post_unmatch_clone;
    }

    public ApplicabilityAnalyzer(String sourcePath, int sourceStart, int sourceEnd, String targetPath, int targetStart, int targetEnd) throws IOException {
        sourceCode = FileUtils.readFileToString(new File((sourcePath)));
        targetCode = FileUtils.readFileToString(new File(targetPath));
//        this.sourceProjectPath = sourceProjectPath;
//        this.targetProjectPath = targetProjectPath;

        CompilationUnit cu1 = JParser.parseCode(sourceCode);
        CloneCalibrator calib1 = new CloneCalibrator(cu1, sourceStart, sourceEnd);
        cu1.accept(calib1);

        CompilationUnit cu2 = JParser.parseCode(targetCode);
        CloneCalibrator calib2 = new CloneCalibrator(cu2, targetStart, targetEnd);
        cu2.accept(calib2);

        //find methods in the given lines of code range
        CloneMethodFinder sourceMethodFinder = new CloneMethodFinder(cu1, sourceStart, sourceEnd);
        cu1.accept(sourceMethodFinder);
        String sourceMethod = sourceMethodFinder.getMethodName();
        //added ability to get owner
        String sourceOwner = sourceMethodFinder.getOwnerName();

        CloneMethodFinder targetMethodFinder = new CloneMethodFinder(cu2, targetStart, targetEnd);
        cu2.accept(targetMethodFinder);
        String targetMethod = targetMethodFinder.getMethodName();
        String targetOwner = targetMethodFinder.getOwnerName();

        // create two refined clones with only graftable code
        int x1 = FileUtiltities.getStartIndex(sourceCode, calib1.first);
        int y1 = FileUtiltities.getEndIndex(sourceCode, calib1.last);
        //sourceClone = new Clone(sourcePath,sourceMethod,x1,y1);
        sourceClone = new Clone(sourceOwner, sourcePath,sourceMethod,x1,y1);

        //sourceClone.setProjectPath(sourceProjectPath);

        int x2 = FileUtiltities.getStartIndex(targetCode, calib2.first);
        int y2 = FileUtiltities.getEndIndex(targetCode, calib2.last);
        //targetClone = new Clone(targetPath,targetMethod,x2,y2);
        targetClone = new Clone(targetOwner, targetPath,targetMethod,x2,y2);
        //targetClone.setProjectPath(targetProjectPath);

        init();

        pre_cm = match(pre1, pre2, sourcePath.equals(targetPath));
        post_cm = match(post1, post2, sourcePath.equals(targetPath));

        prematch = new Matches(pre_cm.matches);
        postmatch = new Matches(post_cm.matches);
        pre_unmatch_clone = pre_cm.unmatches2;
        post_unmatch_clone = post_cm.unmatches2;
//        CompilationUnit cu = JParser.parsePath(sourcePath);
//        MyVisitor visitor = new MyVisitor(sourceClone, cu,sourcePath);
//        visitor.setProjectPath("C:\\Users\\muka\\repos\\featracer");
//        cu.accept(visitor);
//        //+++TEST
//        //print out imports
//        for(String s :visitor.import_decls){
//            System.out.println(s);
//        }

        //test CloneVisitor
//        CompilationUnit cu_2 = JParser.parsePath(targetPath);
//        CloneVisitor cv = new CloneVisitor(targetClone, cu,targetPath);
//        cv.setProjectPath("C:\\Users\\muka\\repos\\featracer");
//        cu_2.accept(cv);
//        System.out.println("====USING CLONEVISITOR====");
//        for(String s :cv.import_decls){
//            System.out.println(s);
//        }
//        System.out.println("\t==FIELDS=");
//        for(String s :cv.field_decls.keySet()){
//            System.out.println(s);
//        }
//        System.out.println("\t==METHODS=");
//        for(Method s :cv.method_decls.keySet()){
//            System.out.println(s.name);
//        }

    }

    private void init() throws IOException{
        DUAnalyzer a1 = new DUAnalyzer(sourceClone);
        DUAnalyzer a2 = new DUAnalyzer(targetClone);
        a1.analyze();
        a2.analyze();

        methodStart = a1.methodStart;
        isMethodEnd = a1.isMethodEnd;
        prestate1 = a1.prestate;
        prestate2 = a2.prestate;
        poststate1 = a1.poststate;
        poststate2 = a2.poststate;
        allstate1 = a1.allstate;
        allstate2 = a2.allstate;
        all1 = a1.all;
        all2 = a2.all;
        pre1 = a1.pre;
        post1 = a1.post;
        pre2 = a2.pre;
        post2 = a2.post;

        cv1 = a1.cv;
        cv2 = a2.cv;
    }

    /**
     * The state-level match is slightly different from the test-level match.
     * Because the test-level match only focuses on the living variables on the entry and exit(s) of a clone, but does not care about the internal ones.
     * Though there is a return statement in the middle of the clone, we do not need to transport the values of internal local variables because the method returns anyway.
     * At such point, we only need to transport the values of affected fields.
     * However, in the state level, the match should consider all variables used by the clone.
     * Because if there is a return statement within the clone, we need to capture all the variables that are alive at the program point of the return statement
     * instead of just at the end of the clone.
     *
     * @return
     */
    public Matches state_level_match(){
        CloneMatcher cm = match(all1, all2, sourceClone.getPath().equals(targetClone.getPath()));
        cm.match();
        return new Matches(cm.matches);
    }
    private HashMap<String, HashSet<Token>> deepCloneHashmap(HashMap<String, HashSet<Token>> vars){
        HashMap<String, HashSet<Token>> set = new HashMap<String, HashSet<Token>>();
        for(String m: vars.keySet()){
            HashSet<Token> tset = new HashSet<Token>(vars.get(m));
            set.put(m, tset);
        }
        return set;
    }
    private CloneMatcher match(HashMap<String, HashSet<Token>> vars1, HashMap<String, HashSet<Token>> vars2, boolean isInSameFile){
        CloneMatcher cm = new CloneMatcher();

        // deep cloning the given two hashmaps before matching, because Grafter will remove certain variables to avoid repetitive matching
        HashMap<String, HashSet<Token>> s1 = deepCloneHashmap(vars1);
        HashMap<String, HashSet<Token>> s2 = deepCloneHashmap(vars2);

        Set<String> mset1 = s1.keySet();
        Set<String> mset2 = s2.keySet();

        HashMap<String, String> m_map = new HashMap<String, String>();
        // try our best to match methods related to the clone at the target location
        for(String m1 : mset1){
            // fuzzy matching
            double max = -1;
            String match = null;
            for(String m2: mset2){
                int distance = StringUtils.getLevenshteinDistance(m1, m2);
                int length = Math.max(m1.length(), m2.length());
                double score = 1 - (double)distance/length;
                if(score > max){
                    match = m2;
                    max = score;
                }
            }

            m_map.put(m1, match);
        }

        // start with the method containing the code clone
        HashSet<Token> tsc1 = s1.get("clone");
        HashSet<Token> tsc2 = s2.get("clone");
        cm.tokens1.addAll(tsc1);
        cm.tokens2.addAll(tsc2);
        CloneMatcher mc = match(tsc1, tsc2);
        cm.matches.addAll(mc.matches);
        cm.unmatches1.addAll(mc.unmatches1);
        cm.unmatches2.addAll(mc.unmatches2);

        // process the callees
        for(String m1 : m_map.keySet()){
            if(m1.equals("clone")) continue;

            String m2 = m_map.get(m1);
            if(isInSameFile && m1.equals(m2)){
                // do not need to match because they are the same method in the same file
                // this is necessary because we have seen cases in Apache Ant where two clones call the same method in the same file
                // and that method uses two variables corresponded between two clones, let's say, excludes and includes,
                // then when matching the same method with itself, includes is matched with itself instead of excludes, which causes trouble.
                continue;
            }

            HashSet<Token> ts1 = s1.get(m1);
            // avoid repetitive matching
            ts1.removeAll(cm.tokens1);
            HashSet<Token> ts2 = s2.get(m_map.get(m1));
            ts2.removeAll(cm.tokens2);
            cm.tokens1.addAll(ts1);
            cm.tokens2.addAll(ts2);
            CloneMatcher m = match(ts1, ts2);
            cm.matches.addAll(m.matches);
            cm.unmatches1.addAll(m.unmatches1);
            cm.unmatches2.addAll(m.unmatches2);
        }

        // put all fields in the unmatched methods in the set of unmatched variables
        HashSet<String> unmatched_methods = new HashSet<String>(mset2);
        unmatched_methods.removeAll(m_map.values());
        for(String m : unmatched_methods){
            HashSet<Token> ts2 = new HashSet<Token>(s2.get(m));
            // remove fields that have already been matched
            ts2.removeAll(cm.tokens2);
            cm.unmatches2.addAll(ts2);
        }

        // Bug fix: in Apache Ant clone pair#18, one clone uses the variable performGc in its containing method
        // but the other clone uses a variable with the same name in a called method. These two variables should be matched.
        // So we need to do another round of matching on the unmatched variables.
        HashSet<Token> tmp1 = new HashSet<Token>();
        HashSet<Token> tmp2 = new HashSet<Token>();
        for(Token t1 : cm.unmatches1){
            for(Token t2 : cm.unmatches2){
                String name1 = t1.getName().replace("_", "");
                String name2 = t2.getName().replace("_", "");
                if(name1.equalsIgnoreCase(name2)){
                    cm.matches.add(new Pair(t1, t2));
                    tmp1.add(t1);
                    tmp2.add(t2);
                }
            }
        }
        cm.unmatches1.removeAll(tmp1);
        cm.unmatches2.removeAll(tmp2);

        return cm;
    }


    private CloneMatcher match(HashSet<Token> s1, HashSet<Token> s2){
        CloneMatcher cm = new CloneMatcher(s1, s2);
        if(cm.match()){
            for(Pair match : cm.matches){
//                if(GrafterConfig.verbose){
//                    System.out.println(match.orign().toString() + " <-> " + match.clone().toString());
//                }
            }
        }else{
            for(Token unmatch : cm.unmatches2){
//                if(GrafterConfig.verbose){
//                    System.out.println(unmatch.getName() + " doesn't have a match.");
//                }
            }
        }

        for(Pair p : cm.matches){
            // redefine matched private fields if they are defined in the super types
            Token t = p.orign();
            Token c = p.clone();
            if(t.isField() && t.isPrivate() && !t.getPath().equals(sourceClone.getPath())){
                try {
                    redefineField(t);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(c.isField() && c.isPrivate() && !c.getPath().equals(targetClone.getPath())){
                try {
                    redefineField(c);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return cm;
    }

    /**
     * It is copied from TranSynthesizer. Therefore it should be updated consistently with the original method.
     *
     * @param t
     * @throws IOException
     */
    private void redefineField(Token t) throws IOException{
        if(t.isPrivate() || t.isProtected()){
            String path = t.getPath();
            String[] src = FileUtiltities.readFileToArray(path);
            for(int i = 0; i < src.length; i++){
                String line = src[i];
                if(line.contains(t.getName()) && line.contains("private")){
                    src[i] = line.replace("private", "public");
                }else if(line.contains(t.getName()) && line.contains("protected")){
                    src[i] = line.replace("protected", "public");
                }
            }

            File bak = new File(path + ".bak");
            if(bak.exists()){
                FileUtiltities.writeStringArraytoFile(src, path);
            }else{
                File old_file = new File(path);
                old_file.renameTo(new File(path + ".bak"));
                File new_file = new File(path);
                new_file.createNewFile();
                FileUtiltities.writeStringArraytoFile(src, path);
            }
        }
    }
}
