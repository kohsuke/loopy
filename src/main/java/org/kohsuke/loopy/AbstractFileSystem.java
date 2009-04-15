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
package org.kohsuke.loopy;

import java.io.RandomAccessFile;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Implementation of FileSystem that is backed by a {@link RandomAccessFile}.
 */
public abstract class AbstractFileSystem implements FileSystem {
    /**
     * Channel to the open file.
     */
    private RandomAccessFile channel;

    private final File file;

    protected AbstractFileSystem(final File file, final boolean readOnly) throws IOException {
        if (!readOnly) {
            throw new IllegalArgumentException("Currrently, only read-only is supported");
        }

        // check that the underlying file is valid
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + file);
        }

        // open the channel
        this.channel = new RandomAccessFile(file, "r");
        this.file = file;
    }

    /**
     * Gets the ISO file that this file system is in.
     */
    public File getIsoFile() {
        return file;
    }

    public FileEntry get(String path) throws IOException {
        return getRootEntry().get(path);
    }

    // TODO: close open streams automatically
    public synchronized void close() throws IOException {
        if (isClosed()) {
            return;
        }

        try {
            this.channel.close();
        }
        finally {
            this.channel = null;
        }
    }

    public boolean isClosed() {
        return (null == this.channel);
    }

    /**
     * Throws an exception if the underlying file is closed.
     *
     * @throws IllegalStateException
     */
    protected void ensureOpen() throws IllegalStateException {
        if (isClosed()) {
            throw new IllegalStateException("File has been closed");
        }
    }

    /**
     * Moves the pointer in the underlying file to the specified position.
     *
     * @param pos
     * @throws IOException
     */
    protected void seek(long pos) throws IOException {
        ensureOpen();
        this.channel.seek(pos);
    }

    /**
     * Reads up to <code>length</code> bytes into the specified buffer, starting at the specified
     * offset. The actual number of bytes read will be less than <code>length</code> if there are
     * not enough available bytes to read, or if the buffer is not large enough.
     *
     * @param buffer
     * @param offset
     * @param length
     * @return the number of bytes read into the buffer
     * @throws IOException
     */
    protected int read(byte[] buffer, int offset, int length) throws IOException {
        ensureOpen();
        return this.channel.read(buffer, offset, length);
    }
}