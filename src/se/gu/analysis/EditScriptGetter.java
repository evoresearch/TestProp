package se.gu.analysis;

import com.github.gumtreediff.actions.Diff;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class EditScriptGetter implements Runnable, Serializable {
private String lineText;
private int line;
ConcurrentHashMap<String,Integer> map;

    public EditScriptGetter(String lineText, int line, ConcurrentHashMap<String, Integer> map) {
        this.lineText = lineText;
        this.line = line;
        this.map = map;
    }

    @Override
    public void run() {
        try {
            String mapKey = String.format("%s;%s", line, lineText);

            String srcFile = lineText.split(";")[6];//source UUT for test case
            String dstFile = lineText.split(";")[14];//target UUT
            Diff d = Diff.compute(srcFile, dstFile);
            int editscriptLength = d.editScript.size();
            map.put(mapKey,editscriptLength);
            System.out.printf("%s;%d\n", mapKey, editscriptLength);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
