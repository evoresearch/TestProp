package se.gu.main;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.locks.StampedLock;

public class TeaCapWriter {
    private StampedLock lock = new StampedLock();
    private StampedLock lock2 = new StampedLock();

    public void writeEditScriptToFile(String line, int editScript, File editScriptsFile) {
        long stamp = lock.writeLock();
        PrintWriter writer=null;
        try {
            writer = new PrintWriter(new FileWriter(editScriptsFile, true));
            writer.printf("%s;%d\n", line, editScript);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(writer!=null){
                writer.close();
            }
            lock.unlockWrite(stamp);
        }
    }

    public void writeVariationToFile(String line, String matches, File editScriptsFile) {
        long stamp = lock.writeLock();
        PrintWriter writer=null;
        try {
            writer = new PrintWriter(new FileWriter(editScriptsFile, true));
            writer.printf("%s;%s\n", line, matches);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(writer!=null){
                writer.close();
            }
            lock.unlockWrite(stamp);
        }
    }

    public void writeFinalVariationToFile(String line, String matches, File editScriptsFile) {
        long stamp = lock2.writeLock();
        PrintWriter writer=null;
        try {
            writer = new PrintWriter(new FileWriter(editScriptsFile, true));
            writer.printf("%s;%s\n", line, matches);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if(writer!=null){
                writer.close();
            }
            lock2.unlockWrite(stamp);
        }
    }
}
