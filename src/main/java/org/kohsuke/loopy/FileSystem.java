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

import java.io.IOException;
import java.io.File;

/**
 * A loopy file system, which is deserialize-only and consists of zero or more entries.
 */
public interface FileSystem {
    /**
     * Returns the directory that represents the root of the file system.
     */
    FileEntry getRootEntry() throws IOException;

    /**
     * Short for {@code getRootEntry().get(path)}.
     */
    FileEntry get(String path) throws IOException;

    /**
     * Short for {@code getRootEntry().grab(path)}.
     */
    FileEntry grab(String path) throws IOException;

    /**
     * Closes this file system. This automatically closes all input streams opened via
     * {@link FileEntry#read()}.
     *
     * @throws IOException if there was an error closing the FileSystem.
     */
    void close() throws IOException;

    /**
     * Returns whether or not this FileSystem has been closed.
     *
     * @return true if {@link FileSystem#close()} has been called on this * FileSystem, otherwise false.
     */
    boolean isClosed();
}