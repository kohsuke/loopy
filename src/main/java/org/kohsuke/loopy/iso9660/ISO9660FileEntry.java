/*
Copyright (C) 2006-2007 loopy project (http://loopy.sourceforge.net)

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/
package org.kohsuke.loopy.iso9660;

import org.kohsuke.loopy.FileEntry;
import org.kohsuke.loopy.rr.RockRidge;
import org.kohsuke.loopy.rr.NMRecord;
import org.kohsuke.loopy.rr.SLRecord;
import org.kohsuke.loopy.util.LittleEndian;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Collections;

/**
 * Represents a file in an ISO9660 file system.
 */
public final class ISO9660FileEntry implements FileEntry {
    public static final char ID_SEPARATOR = ';';

    private ISO9660FileSystem fileSystem;
    private String parentPath;
    private final int entryLength;
    private final long startSector;
    private final int dataLength;
    private final long lastModifiedTime;
    private final int flags;
    private final String identifier;
    private final byte[] systemUse;
    public final int extAttributeLength;

    //private final int extAttributeLength;
    //private final int fileUnitSize;
    //private final int interleaveSize;

    /**
     * If this file entry has RR extensions, load them here.
     */
    private final List<RockRidge> rr;


    public ISO9660FileEntry(final ISO9660FileSystem fileSystem, final byte[] block, final int pos) throws IOException {
        this(fileSystem, null, block, pos);
    }

    /**
     * Initialize this instance.
     * @param fileSystem the parent file system
     * @param parentPath the path of the parent directory
     * @param block the bytes of the sector containing this file entry
     * @param startPos the starting position of this file entry
     */
    public ISO9660FileEntry(final ISO9660FileSystem fileSystem, final String parentPath,
                            final byte[] block, final int startPos) throws IOException {
        this.fileSystem = fileSystem;
        this.parentPath = parentPath;

        final int offset = startPos - 1;

        this.entryLength = Util.getUInt8(block, offset+1);
        this.extAttributeLength = Util.getUInt8(block, offset+2);
        this.startSector = Util.getUInt32LE(block, offset+3);
        this.dataLength = (int) Util.getUInt32LE(block, offset+11);
        this.lastModifiedTime = Util.getDateTime(block, offset+19);
        this.flags = Util.getUInt8(block, offset + 26);
        //this.fileUnitSize = Util.getUInt8(block, offset+27);
        //this.interleaveSize = Util.getUInt8(block, offset+28);
        this.identifier = getFileIdentifier(block, offset, isDirectory());

        int header = padToEven(33+Util.getUInt8(block,offset+33));
        if(entryLength==header) {
            systemUse = EMPTY_ARRAY;
            rr = Collections.emptyList();
        } else {
            systemUse = new byte[entryLength-header];
            System.arraycopy(block,offset+header,systemUse,0,systemUse.length);
            rr = RockRidge.parse(systemUse);
        }
    }

    private int padToEven(int len) {
        if(len%2==0)    return len;
        return len+1;
    }

    private String getFileIdentifier(final byte[] block, final int offset, final boolean isDir) {
        final int fidLength = Util.getUInt8(block, offset+33);

        if (isDir) {
            final int buff34 = Util.getUInt8(block, offset+34);

            if ((fidLength == 1) && (buff34 == 0x00)) {
                return ".";
            }
            else if ((fidLength == 1) && (buff34 == 0x01)) {
                return "..";
            }
        }

        final String id = Util.getDChars(
                block, offset+34, fidLength, this.fileSystem.getEncoding());

        final int sepIdx = id.indexOf(ID_SEPARATOR);

        if (sepIdx >= 0) {
            return id.substring(0, sepIdx);
        }
        else {
            return id;
        }
    }

    public String getName() {
        NMRecord nm = getRockRidgeExtension(NMRecord.class);
        if(nm!=null)    return nm.name;
        return this.identifier;
    }

    public String getPath() {
        if (".".equals(this.getName())) {
            return "";
        }

        StringBuffer buf = new StringBuffer();

        if (null != this.parentPath) {
            buf.append(this.parentPath);
        }

        buf.append(getName());

        if (isDirectory()) {
            buf.append("/");
        }

        return buf.toString();
    }

    public long getLastModifiedTime() {
        return this.lastModifiedTime;
    }

    public boolean isDirectory() {
        return (this.flags & 0x03) != 0;
    }

    public String getSymLinkTarget() {
        SLRecord r = getRockRidgeExtension(SLRecord.class);
        if(r!=null) return r.name;
        return null;
    }

    public int getSize() {
        return this.dataLength;
    }

    /**
     * Returns the block number where this entry starts.
     *
     * @return
     */
    public long getStartBlock() {
        return this.startSector;
    }

    /**
     * Returns the size this entry takes up in the file table.
     *
     * @return
     */
    public int getEntryLength() {
        return this.entryLength;
    }

    /**
     * Returns true if this is the last entry in the file system.
     *
     * @return
     */
    public final boolean isLastEntry() {
        return (this.flags & 0x40) == 0;
    }

    public InputStream read() {
        return new EntryInputStream(this, fileSystem);
    }

    public LinkedHashMap<String,FileEntry> childEntries() throws IOException {
        LinkedHashMap<String,FileEntry> children = new LinkedHashMap<String, FileEntry>();

        if(!isDirectory())
            return children;

        final byte[] content = this.fileSystem.getBytes(this);

        int offset = 0;
        boolean paddingMode = false;

        while (offset < content.length) {
            if (LittleEndian.getUInt8(content, offset) <= 0) {
                paddingMode = true;
                offset += 2;
                continue;
            }

            ISO9660FileEntry child = new ISO9660FileEntry(
                    this.fileSystem, getPath(), content, offset+1);

            if (paddingMode && child.getSize() < 0) {
                continue;
            }

            offset += child.getEntryLength();

            children.put(child.getName(),child);
        }

        return children;
    }

    public FileEntry get(String path) throws IOException {
        FileEntry cur=this;
        while(cur!=null) {
            if(path.length()==0)
                return cur;
            if(path.startsWith("/")) {
                path = path.substring(1);
                cur=fileSystem.getRootEntry();
                continue;
            }

            int idx = path.indexOf('/');
            if(idx<0)
                return cur.childEntries().get(path);

            cur=cur.childEntries().get(path.substring(0,idx));
            path = path.substring(idx+1);
        }
        return null; // not found
    }

    public FileEntry grab(String path) throws IOException {
        FileEntry e = get(path);
        if(e==null) throw new IOException(path+" not found in "+getPath()+" on "+fileSystem.getIsoFile());
        return e;
    }

    public byte[] getSystemUse() {
        return systemUse;
    }

    public <T extends RockRidge> T getRockRidgeExtension(Class<T> type) {
        for (RockRidge r : rr)
            if(r.getClass()==type)
                return type.cast(r);
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ISO9660FileEntry that = (ISO9660FileEntry) o;

        if (!fileSystem.equals(that.fileSystem)) return false;
        if (!identifier.equals(that.identifier)) return false;
        if (parentPath != null ? !parentPath.equals(that.parentPath) : that.parentPath != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = fileSystem.hashCode();
        result = 31 * result + (parentPath != null ? parentPath.hashCode() : 0);
        result = 31 * result + identifier.hashCode();
        return result;
    }

    private static final byte[] EMPTY_ARRAY = new byte[0];
}