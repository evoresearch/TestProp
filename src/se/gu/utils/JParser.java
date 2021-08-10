package se.gu.utils;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class JParser {
    public static CompilationUnit parseCode(String code){

        return getCompilationUnit(code);
    }
    public static CompilationUnit parsePath(String path) throws IOException {

        String code = FileUtils.readFileToString(new File(path));
        return getCompilationUnit(code);
    }

    private static CompilationUnit getCompilationUnit(String code) {
        ASTParser parser = ASTParser.newParser(AST.JLS14);
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
        parser.setCompilerOptions(options);
        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        return cu;
    }
}
