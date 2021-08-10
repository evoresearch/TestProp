package se.gu.main;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import se.gu.analysis.*;
import se.gu.model.Clone;
import se.gu.model.Matches;
import se.gu.model.Method;
import se.gu.model.Token;
import se.gu.utils.FileUtiltities;
import se.gu.utils.JParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This class analyzes two code clones to find any variations in their variables and assess whether a test case
 * from the source clone can be applied to the target clone
 * @author Mukelabai Mukelabai
 * @date 2021-08-10
 *
 * Code is inspired by work from Tianyi Zhang
 */
public class ApplicabilityAnalyzer {
    private String sourceCode, targetCode;
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

        CloneMethodFinder targetMethodFinder = new CloneMethodFinder(cu2, targetStart, targetEnd);
        cu2.accept(targetMethodFinder);
        String targetMethod = targetMethodFinder.getMethodName();

        // create two refined clones with only graftable code
        int x1 = FileUtiltities.getStartIndex(sourceCode, calib1.first);
        int y1 = FileUtiltities.getEndIndex(sourceCode, calib1.last);
        sourceClone = new Clone(sourcePath,sourceMethod,x1,y1);

        int x2 = FileUtiltities.getStartIndex(sourceCode, calib2.first);
        int y2 = FileUtiltities.getEndIndex(sourceCode, calib2.last);
        targetClone = new Clone(targetPath,targetMethod,x2,y2);

        //init();
        CompilationUnit cu = JParser.parsePath(sourcePath);
        MyVisitor visitor = new MyVisitor(sourceClone, cu,sourcePath);
        visitor.setProjectPath("C:\\Users\\muka\\repos\\featracer");
        cu.accept(visitor);
        //+++TEST
        //print out imports
        for(String s :visitor.import_decls){
            System.out.println(s);
        }

        //test CloneVisitor
        CompilationUnit cu_2 = JParser.parsePath(targetPath);
        CloneVisitor cv = new CloneVisitor(targetClone, cu,targetPath);
        cv.setProjectPath("C:\\Users\\muka\\repos\\featracer");
        cu_2.accept(cv);
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

}
