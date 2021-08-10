package se.gu.analysis;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.*;
import se.gu.model.Clone;
import se.gu.model.Label;
import se.gu.model.Method;
import se.gu.model.Token;
import se.gu.utils.FileUtiltities;
import se.gu.utils.JParser;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MyVisitor extends ASTVisitor {
    CompilationUnit cu;
    Clone clone;
    public String path;
    public HashSet<String> import_decls = new HashSet<String>();
    ArrayList<Token> fields;
    public HashMap<String, FieldDeclaration> field_decls = new HashMap<String, FieldDeclaration>();
    public HashMap<Method, MethodDeclaration> method_decls = new HashMap<Method, MethodDeclaration>();

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    private String projectPath;
    ArrayList<Token> vars; // all local variables in the code clone
    Stack<Pair<Integer, Integer>> blockStacks = new Stack<Pair<Integer, Integer>>();
    public int methodStart = 0; // the line number of the last field declaration
    public boolean isMethodEnd = false;

    public MyVisitor(Clone clone, CompilationUnit cu, String path) {
        this.cu = cu;
        this.clone = clone;
        this.path = path;
        fields = new ArrayList<>();
    }
    private int getLineNumber(ASTNode node){
        return cu.getLineNumber(node.getStartPosition());
    }
    @Override
    public boolean visit(ImportDeclaration node) {
        this.import_decls.add(node.toString());
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        String name = node.getName().getIdentifier();
        //System.out.println(name);
        ArrayList<String> args = new ArrayList<String>();
        List l = node.parameters();
        for (Object o : l) {
            if (o instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
                args.add(svd.getType().toString());
            }
        }
        Method m = new Method(name, args);
        method_decls.put(m, node);

        // only analyze the method that contains code clones, also need to check the location of method since there might be overwritten methods
        if (name.equals(clone.getMethod()) && node.getStartPosition() <= clone.getX() && node.getStartPosition() + node.getLength() >= clone.getY()) {
            int start = cu.getLineNumber(node.getStartPosition());
            int end = cu.getLineNumber(node.getStartPosition() + node.getLength());

            // the method containing the clone is the biggest block and should be pushed to the blockStack first
            blockStacks.push(new ImmutablePair<Integer, Integer>(start, end));

            // set method start
            this.methodStart = start;

            // check if clone is at the end of the method
            if (end == clone.getEnd() + 1) {
                this.isMethodEnd = true;
            }

            // check if it is static
            boolean isStatic = false;
            List mods = node.modifiers();
            for (Object obj : mods) {
                if (obj instanceof Modifier) {
                    String mod = ((Modifier) obj).toString();
                    if (mod.equals("static")) {
                        isStatic = true;
                        this.clone.setStatic(isStatic);
                        ;
                    }
                }
            }
            if (node.getReturnType2() == null) {
                // this is an constructor
                clone.setType("");
            } else {
                clone.setType(node.getReturnType2().toString());
            }

            // get thrown exceptions
            ArrayList<String> excps = new ArrayList<String>();
            for (Object obj : node.thrownExceptionTypes()) {
                excps.add(obj.toString());
//                if (obj instanceof Name) {
//                    excps.add(((Name) obj).toString());
//                }
            }
            clone.setExceptions(excps);

            ArrayList<Token> params = new ArrayList<Token>();
            for (Object obj : node.parameters()) {
                SingleVariableDeclaration decl = (SingleVariableDeclaration) obj;
                String arg = decl.getName().getIdentifier();
                String type = decl.getType().toString();
                Token t = new Token(type, Label.LOCAL, arg);
                int line = getLineNumber(decl);
                t.start = line; // the parameter is alive starting from the line it is declared
                t.end = end - 1; // the parameter is dead at the end of the method
                vars.add(t);
                params.add(t);
            }
            clone.setParameters(params);

            node.getBody().accept(this);

            blockStacks.pop();
        }

        return false;
    }


    @Override
    public boolean visit(FieldDeclaration node) {
        String type = node.getType().toString();

        boolean isFinal = false;
        boolean isPrivate = false;
        boolean isStatic = false;
        boolean isProtected = false;

        // check if it is final and private
        List mods = node.modifiers();
        for (Object obj : mods) {
            if (obj instanceof Modifier) {
                String mod = ((Modifier) obj).toString();
                if (mod.equals("final")) {
                    isFinal = true;
                } else if (mod.equals("private")) {
                    isPrivate = true;
                } else if (mod.equals("protected")) {
                    isProtected = true;
                } else if (mod.equals("static")) {
                    isStatic = true;
                }
            }
        }

        Iterator<?> iter = node.fragments().iterator();
        while (iter.hasNext()) {
            VariableDeclarationFragment f = (VariableDeclarationFragment) iter.next();
            SimpleName var = f.getName();
            String name = var.getIdentifier();

            // check if the field is initialized
            // check if the field is initialized
            boolean isInit = false;
            String init = null;
            if (f.getInitializer() != null) {
                isInit = true;
                init = f.getInitializer().toString();
            }

            Token t = new Token(type, Label.FIELD, name, isFinal, isPrivate, isProtected, isInit);
            fields.add(t);
            t.setInit(init);
            t.isStatic = isStatic;
            t.setPath(this.path);
            this.field_decls.put(t.getName(), node);
        }

        return false;
    }

}
