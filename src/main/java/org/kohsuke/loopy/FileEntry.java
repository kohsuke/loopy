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

import java.io.InputStream;
import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * An entry in a loopy file system. Paths within the file system are unix-like ("/" separated
 * hierarchy of folders and files).
 */
public interface FileEntry {
    /**
     * Returns the entry name (the last entry of the path).
     *
     * @return the entry name
     */
    String getName();

    /**
     * Returns the relative entry path. The path does NOT begin with any separators.
     *
     * @return the entry path
     */
    String getPath();

    /**
     * Returns the last modified time for this entry, in milliseconds.
     *
     * @return the last modified time
     */
    long getLastModifiedTime();

    /**
     * Returns whether this entry represents a directory.
     *
     * @return true if this entry represents a directory, otherwise false.
     */
    boolean isDirectory();

    /**
     * Returns the size, in bytes, of the data represented by this entry.
     *
     * @return the entry size
     */
    int getSize();

    /**
     * Returns an input stream that reads the data for the given entry.
     *
     * @return an input stream that reads the contents of the given entry
     */
    InputStream read();

    /**
     * If this is a directory, returns all the children, including sub-directories and files,
     * as well as "." and "..".
     *
     * @return
     *      Never null. Ordered as listed in the the directory entry.
     */
    LinkedHashMap<String,FileEntry> childEntries() throws IOException;
}