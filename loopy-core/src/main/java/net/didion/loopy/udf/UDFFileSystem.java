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
package net.didion.loopy.udf;

import net.didion.loopy.AbstractBlockFileSystem;
import net.didion.loopy.VolumeDescriptorSet;
import net.didion.loopy.FileEntry;
import net.didion.loopy.iso9660.Constants;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;

public class UDFFileSystem extends AbstractBlockFileSystem {
    public UDFFileSystem(File file, boolean readOnly) throws IOException {
        this(file, readOnly, Constants.DEFAULT_BLOCK_SIZE);
    }

    public UDFFileSystem(File file, boolean readOnly, int sectorSize) throws IOException {
        super(file, readOnly, sectorSize, Constants.RESERVED_SECTORS);
    }

    public InputStream getInputStream(FileEntry entry) {
        return null;
    }

    protected Enumeration enumerate(FileEntry root) {
        return null;
    }

    protected VolumeDescriptorSet createVolumeDescriptorSet() {
        return new UDFVolumeDescriptorSet(this);
    }
}