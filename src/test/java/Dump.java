import org.kohsuke.loopy.FileEntry;
import org.kohsuke.loopy.iso9660.ISO9660FileSystem;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * @author Kohsuke Kawaguchi
 */
public class Dump {
    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            ISO9660FileSystem fs = new ISO9660FileSystem(new File(arg), false);
            list(fs.getRootEntry(),0);
            fs.close();
        }
    }

    private static void list(FileEntry e, int indent) throws IOException {
        for( int i=0; i<indent; i++ )
            System.out.print("  ");

        String dst = e.getSymLinkTarget();
        int width = 60-e.getName().length()-indent*2;
        System.out.print(e.getName());

        for( int i=width; i>0; i-- )
            System.out.print(" ");
        // compare time with "ls --time-style=+%s -la ."
        System.out.printf("%10d %s %s\n",e.getSize(), e.getLastModifiedTime(), dst==null?"":" -> "+dst);

        if(e.isDirectory())
            for (FileEntry f : e.childEntries().values()) {
                if(f.getName().equals(".") || f.getName().equals(".."))
                    continue;
                list(f,indent+1);
            }
    }

    private static final DateFormat df = SimpleDateFormat.getDateTimeInstance();
    static {
        df.setTimeZone(TimeZone.getTimeZone("PDT"));
    }
}
