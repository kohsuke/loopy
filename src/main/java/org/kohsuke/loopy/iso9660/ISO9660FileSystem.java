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

import org.kohsuke.loopy.AbstractBlockFileSystem;
import org.kohsuke.loopy.VolumeDescriptorSet;

import java.io.File;
import java.io.IOException;

public class ISO9660FileSystem extends AbstractBlockFileSystem {
    final boolean readJoliet;

    /**
     * @param readJoliet
     *      True to read supplementary volume descriptors. AFAIK, only Joliet uses it,
     *      so this is equivalent to prefer joliet (and ignore rock ridge extensions,
     *      which means all the POSIX like information will be lost.)
     */
    public ISO9660FileSystem(File file, boolean readJoliet) throws IOException {
        super(file, true, Constants.DEFAULT_BLOCK_SIZE, Constants.RESERVED_SECTORS);
        this.readJoliet = readJoliet;
    }

    public String getEncoding() {
        return ((ISO9660VolumeDescriptorSet) getVolumeDescriptorSet()).getEncoding();
    }

    byte[] getBytes(ISO9660FileEntry entry) throws IOException {
        int size = entry.getSize();

        byte[] buf = new byte[size];

        readBytes(entry, 0, buf, 0, size);

        return buf;
    }

    int readBytes(ISO9660FileEntry entry, int entryOffset, byte[] buffer, int bufferOffset, int len)
            throws IOException {
        long startPos = (entry.getStartBlock() * Constants.DEFAULT_BLOCK_SIZE) + entryOffset;
        return readData(startPos, buffer, bufferOffset, len);
    }

    protected VolumeDescriptorSet createVolumeDescriptorSet() {
        return new ISO9660VolumeDescriptorSet(this);
    }
}
