package org.kohsuke.loopy.rr;

/**
 * Symlink.
 *
 * @author Kohsuke Kawaguchi
 */
public class SLRecord extends RockRidge {
    public final String name;

    public SLRecord(String name) {
        this.name = name;
    }
}
