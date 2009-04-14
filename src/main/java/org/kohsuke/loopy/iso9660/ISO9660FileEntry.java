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
import org.kohsuke.loopy.util.LittleEndian;

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

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

    //private final int extAttributeLength;
    //private final int fileUnitSize;
    //private final int interleaveSize;

    public ISO9660FileEntry(final ISO9660FileSystem fileSystem, final byte[] block, final int pos) {
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
                            final byte[] block, final int startPos) {
        this.fileSystem = fileSystem;
        this.parentPath = parentPath;

        final int offset = startPos - 1;

        this.entryLength = Util.getUInt8(block, offset+1);
        //this.extAttributeLength = Util.getUInt8(block, offset+2);
        this.startSector = Util.getUInt32LE(block, offset+3);
        this.dataLength = (int) Util.getUInt32LE(block, offset+11);
        this.lastModifiedTime = Util.getDateTime(block, offset+19);
        this.flags = Util.getUInt8(block, offset + 26);
        //this.fileUnitSize = Util.getUInt8(block, offset+27);
        //this.interleaveSize = Util.getUInt8(block, offset+28);
        this.identifier = getFileIdentifier(block, offset, isDirectory());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ISO9660FileEntry that = (ISO9660FileEntry) o;
        return fileSystem.equals(that.fileSystem) && identifier.equals(that.identifier) && parentPath.equals(that.parentPath);

    }

    @Override
    public int hashCode() {
        int result = fileSystem.hashCode();
        result = 31 * result + parentPath.hashCode();
        result = 31 * result + identifier.hashCode();
        return result;
    }
}