package se.gu.analysis;

import org.eclipse.jdt.core.dom.*;

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

    private String methodName,ownerName;

    public boolean visit(MethodDeclaration node){
        int nodeStartPosition = node.getStartPosition();
        int nodeStart = cu.getLineNumber(nodeStartPosition);
        int nodeEnd = cu.getLineNumber(nodeStartPosition+node.getLength());
        //if the method declaration begins before or at the line number given
        //and the method ends after or at the line given or the differene between method end and line given is not more than 3 lines of code,
        // then we have found our method
        if(nodeStart<=codeStart&&(nodeEnd>=codeEnd||Math.abs(nodeEnd-codeEnd)<=3)){
            setMethodName(node.getName().toString());

            String owner = getOwner((TypeDeclaration) node.getParent() );

            setOwnerName(owner);
        }
        return false;
    }
private String getOwner(TypeDeclaration node){
        return node.getName().toString();
}
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
