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

public class Main {
public static void main(String[]args){
    try {
        Run.initGenerators(); // registers the available parsers
        String srcFile = "C:/testpropagation/clones/robotmedia/AndroidBillingLibrary/AndroidBillingLibraryTest/src/net/robotmedia/billing/helper/AbstractBillingActivityTest.java";
        String dstFile = "C:/testpropagation/clones/serso/android-billing/core_testing/src/test/java/net/robotmedia/billing/helper/AbstractBillingActivityTest.java";
        Tree src =  //new JdtTreeGenerator().generateFrom().file(srcFile).getRoot();
                TreeGenerators.getInstance().getTree(srcFile).getRoot(); // retrieves and applies the default parser for the file
        Tree dst = TreeGenerators.getInstance().getTree(dstFile).getRoot(); // retrieves and applies the default parser for the file
        Matcher defaultMatcher = Matchers.getInstance().getMatcher(); // retrieves the default matcher
        MappingStore mappings = defaultMatcher.match(src, dst); // computes the mappings between the trees
        EditScriptGenerator editScriptGenerator = new SimplifiedChawatheScriptGenerator(); // instantiates the simplified Chawathe script generator
        EditScript actions = editScriptGenerator.computeActions(mappings); // computes the edit script
    }catch (Exception ex){
        ex.printStackTrace();
    }
}
}
