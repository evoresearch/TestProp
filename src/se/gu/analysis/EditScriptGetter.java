package se.gu.analysis;

import com.github.gumtreediff.actions.Diff;
import se.gu.config.Configuration;
import se.gu.main.TeaCapWriter;

import java.io.File;
import java.io.Serializable;

public class EditScriptGetter implements Runnable, Serializable {
    private String lineText;
    private int line;
    private Configuration configuration;
    //ConcurrentHashMap<String,Integer> map;
    private TeaCapWriter teaCapWriter;
    private File editScriptFile;

    public EditScriptGetter(String lineText, int line, TeaCapWriter teaCapWriter, File editScriptFile, Configuration config) {
        this.lineText = lineText;
        this.line = line;
        //this.map = map;
        this.teaCapWriter = teaCapWriter;
        this.editScriptFile = editScriptFile;
        this.configuration = config;

    }

    @Override
    public void run() {
        try {
            String mapKey = String.format("%s;%s", line, lineText);
            String[] items = lineText.split(";");
            String srcFile = items[configuration.getIndexOfSourceFile()];//source UUT for test case
            String dstFile = items[configuration.getIndexOfTargetUUTFile()];//target UUT
            Diff d = Diff.compute(srcFile, dstFile);
            int editscriptLength = d.editScript.size();
            teaCapWriter.writeEditScriptToFile(mapKey, editscriptLength, editScriptFile);
            //map.put(mapKey,editscriptLength);
            System.out.printf("%s;%d\n", mapKey, editscriptLength);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
