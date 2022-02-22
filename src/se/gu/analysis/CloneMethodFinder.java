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

            String owner = getOwner( node.getParent() );

            setOwnerName(owner);
        }
        return false;
    }
private String getOwner(ASTNode node){
        String owner = null;
        if(node instanceof EnumDeclaration){
            owner = ((EnumDeclaration)node).getName().toString();
        }else{
            owner = ((TypeDeclaration)node).getName().toString();
        }
        return owner;
}
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
