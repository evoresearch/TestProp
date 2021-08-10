package se.gu.analysis;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class CloneMethodFinder extends ASTVisitor {
    public CloneMethodFinder(CompilationUnit cu, int codeStart, int codeEnd) {
        this.cu = cu;
        this.codeStart = codeStart;
        this.codeEnd = codeEnd;
    }

    private CompilationUnit cu;
    private int codeStart,codeEnd;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    private String methodName;

    public boolean visit(MethodDeclaration node){
        int nodeStartPosition = node.getStartPosition();
        int nodeStart = cu.getLineNumber(nodeStartPosition);
        int nodeEnd = cu.getLineNumber(nodeStartPosition+node.getLength());
        //if the method declaration begins before or at the line number given
        //and the method ends after or at the line given or the differene between method end and line given is not more than 3 lines of code,
        // then we have found our method
        if(nodeStart<=codeStart&&(nodeEnd>=codeEnd||Math.abs(nodeEnd-codeEnd)<=3)){
            setMethodName(node.getName().toString());
        }
        return false;
    }
}
