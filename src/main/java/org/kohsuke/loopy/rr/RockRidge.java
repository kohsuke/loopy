package org.kohsuke.loopy.rr;

import org.kohsuke.loopy.iso9660.Util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Rock ridge extension.
 * 
 * @author Kohsuke Kawaguchi
 */
public class RockRidge {
    public static List<RockRidge> parse(byte[] data) throws IOException {
        List<RockRidge> r = new ArrayList<RockRidge>();
        DataInputStream di = new DataInputStream(new ByteArrayInputStream(data));
        String symlink = "";

        while (di.available()>=4) {
            String key = new String(new byte[]{di.readByte(),di.readByte()});
            int len = di.readByte()-4; // payload length
            di.readByte(); // version. ignored.

            if(key.equals("NM")) {
                len--;
                di.readByte(); // flag. ignored
                byte[] buf = new byte[len];
                di.readFully(buf);
                r.add(new NMRecord(new String(buf)));
            } else
            if(key.equals("SL")) {
                len--;
                di.readByte(); // flag. ignored

                byte[] components = new byte[len];
                di.readFully(components);


                DataInputStream ci = new DataInputStream(new ByteArrayInputStream(components));
                while(ci.available()>0) {
                    int cf = ci.readByte(); // flag
                    int clen = ci.readByte(); // length
                    byte[] buf = new byte[clen];
                    ci.readFully(buf);
                    switch (cf&0xFE) {
                    case 0:
                        symlink+=new String(buf);
                        break;
                    case 2:
                        symlink+=".";
                        break;
                    case 4:
                        symlink+="..";
                        break;
                    case 8:
                        symlink+="/";
                        break;
                    }

                    if((cf&1)==0 && !symlink.endsWith("/"))
                        symlink+='/';
                }
            } else
            if(key.equals("TF")) {
                len--;
                int flags = di.readByte();
                boolean longForm = (flags&0x80)!=0;
                flags&=0x7F;
                long[] timestamps = new long[7];
                for(int i=0; i<7; i++) {
                    if((flags&1)==0)  continue; // skip
                    flags >>= 1;

                    if(longForm) {
                        byte[] buf = new byte[17];
                        di.readFully(buf);
                        timestamps[i] = Util.getStringDate(buf,1);
                    } else {
                        byte[] buf = new byte[7];
                        di.readFully(buf);
                        timestamps[i] = Util.getDateTime(buf,1);
                    }
                }
                r.add(new TFRecord(timestamps));
            } else {
                // unknown just skip it
                di.skipBytes(len);
            }
        }

        if(symlink.endsWith("/"))    symlink=symlink.substring(0,symlink.length()-1);
        if(symlink.length()>0)
            r.add(new SLRecord(symlink));

        return r;
    }

    private static int b2i(byte b) {
        if(b>=0) return b;
        return b+256;
    }
}
